 
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 import javax.swing.GroupLayout;
 import javax.swing.JComponent;
 import javax.swing.JTextArea;
 import java.awt.Font;
 import javax.swing.WindowConstants;
 import java.awt.event.*;
 
 import java.util.*;
 import java.io.*;
 
 
 class DCPU16Screen
 	extends JFrame
 	implements Runnable
 {
 
 	static final long serialVersionUID = 1;
 
 	private Memory m;
 	private int beginning;
 	private boolean stop;
 	private boolean running;
 	private int[] lastBytes;
 
 	private JTextArea screenTextArea;
 
 	public DCPU16Screen(Memory memory, int beginning){
 		this.m = memory;
 		this.beginning = beginning;
 	}
 
 	public DCPU16Screen(Memory memory){
 		this.m = memory;
 		this.beginning = 0x8000;
 	}
 
 	private void doShow(){
 		SwingUtilities.invokeLater(new Runnable(){
 			public void run(){
 				showMe();
 			}
 		});
 	}
 
 	private void showMe(){
 		GroupLayout thisLayout = new GroupLayout((JComponent)getContentPane());
 		getContentPane().setLayout(thisLayout);
 		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
 		{
 			screenTextArea = new JTextArea();
 			screenTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
 			screenTextArea.setEditable(false);
 		}
 		thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
 			.addComponent(screenTextArea, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE)
 			);
 		thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup()
 			    .addComponent(screenTextArea, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE)
 			    );
 
 		pack();
 		setSize(300, 300);
 		setLocationRelativeTo(null);
 		setVisible(true);
 
 		addWindowListener(new WindowAdapter(){
 			public void windowClosing(WindowEvent e){
 				stop();
 			}
 		});
 	}
 
 	public void run(){
 		doShow();
 		running = true;
 		synchronized(this){
 			while (true){
 				if (stop)
 					break;
 				if (! refresh()) // if refresh does something, don't wait, go around again immediately
 					try{
 						wait(100);
 					}
 					catch (InterruptedException e){}
 			}
 		}
 		running = false;
 	}
 
 	public void stop(){
 		if (running)
 			stop = true;
 	}
 
 	private boolean sameBytes(int[] a, int[] b){
 		if (a.length != b.length)
 			return false;
 		for (int i = 0; i < a.length; i++)
 			if (a[i] != b[i])
 				return false;
 		return true;
 	}
 
 	public boolean refresh(){
 		if (screenTextArea == null)
 			return false;
 		int[] bytes = m.read(beginning, 32 * 12);
 		if (lastBytes == null || ! sameBytes(bytes, lastBytes))
 			lastBytes = bytes;
 		else
 			return false;
 		String screen = "";
 		for (int i = 0; i < bytes.length; i++){
 			if (i % 32 == 0)
 				screen += Hexer.hex(i / 32) + ": ";
 			char c = (char)(bytes[i] & 0xFF);
 			screen += c == 0 ? 0x20 : c;
 			if (i % 32 == 31)
 				screen += "\n";
 		}
 		screen += "\n";
 		screenTextArea.setText(screen);
 		return true;
 	}
 
 	public static void main(String[] args){
 		if (args.length < 1){
 			System.out.println("Call me with a filename to run");
 			return;
 		}
 		final DCPU16 d = new DCPU16();
 		//d.setDebug(true);
 		d.setSpeed(100000);
 		loadFromFile(args[0], d.memory());
 		new Thread(new DCPU16Screen(d.memory())).start();
 		new Thread(new Runnable(){
 			public void run(){
 				synchronized(this){
					while (true){
 						System.out.println(d.dump());
 						try{
 							wait(1000);
 						}
 						catch(InterruptedException e){}
 					}
 				}
 			}
 		}).start();
		d.run();
 	}
 
 	private static void loadFromFile(String filename, Memory memory){
 		try{
 			File file = new File(filename);
 			FileInputStream reader = new FileInputStream(file);
 			byte[] octets = new byte[(int)file.length()];
 			reader.read(octets);
 			int[] sextets = new int[octets.length / 2];
 			for (int i = 0; i < sextets.length; i++)
 				sextets[i] = ((int)(octets[i * 2] & 0xFF) << 8) | (int)(octets[i * 2 + 1] & 0xFF);
 			memory.write(0, sextets);
 		}
 		catch (FileNotFoundException e){
 			System.out.println("" + e);
 		}
 		catch (IOException e){
 			System.out.println("" + e);
 		}
 	}
 }
