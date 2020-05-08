 import java.awt.*;
 import java.io.*;
 import javax.swing.*;
 import java.awt.event.*;
 import java.lang.Thread.*;
 import java.util.*;
 public class connectui extends JFrame
 {
 	JLabel status = new JLabel("Avviato");
 	String stmnt="";
 	boolean runn = false;
 	JButton con = new JButton("Connetti");
 	JButton dis = new JButton("Disconnetti");
 	JPanel pan = new JPanel();
 	BorderLayout br = new BorderLayout();
 	FlowLayout fl = new FlowLayout();
	JList apn = listalo();
 	ArrayList<String> apn_list = new ArrayList<String>();
 	SimpleRunner r = new SimpleRunner();
 	Thread statuses = new Thread(r);
 	
 	public connectui()
 	{
 		super("connectUI");
 		pan.setLayout(fl);
 		pan.add(con);
 		pan.add(dis);
 		add(pan,br.NORTH);
 		add(status,br.SOUTH);
 		actions();
 		statuses.start();
		add(apn,br.CENTER);
 		pack();
 		show();
 	} 
 	
 	void actions()
 	{
 		con.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				connect();
 			}
 		});
 		dis.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				disconnect();
 			}
 		});
 	}
 	
 	void connect()
 	{
 		Process con;
		stmnt ="sakis3gz connect APN="+apn.getSelectedValue().toString(); // that's the command that connect to the internet
 		System.out.print(stmnt);
 		try{con = Runtime.getRuntime().exec(stmnt); 
 		status.setText("Connecting");
 		runn=true;
 		}
 		catch(Exception err){status.setText("Error-Disconnected");}
 		runn=false;
 		
 		
 	}	
 		
 	void disconnect()
 	{
 		Process dis;
 		stmnt="sakis3gz disconnect";
 		try{dis = Runtime.getRuntime().exec(stmnt); status.setText("Disconnetting"); runn=true;}
 		catch(Exception err){status.setText("Error-Connected");}
 		runn=false;
 	}
 	
 	public ArrayList<String> apns_enum() throws IOException //It opens  a file called apns_list located into the directory of the .java file. That file contains
 	{														// a list of apns that will be passed to the JList. It's simple for you adding others apns.
 		File apns_file = new File("apns_list.txt");
 		BufferedReader reader = null;
 		ArrayList<String> tmp = new ArrayList<String>();
 		if (!apns_file.exists()) {status.setText("apns file not found, create one please"); System.exit(0);}
 		try {
 				FileInputStream in = new FileInputStream(apns_file);
 				reader= new BufferedReader(new InputStreamReader(in));
 				String tmps = null;
 				while((tmps = reader.readLine()) !=null) {tmp.add(tmps);}
 			}
 		finally {reader.close();}
 		return tmp;
 	}
	public JList listalo()
 	{
 		try{apn_list=apns_enum();} catch (IOException errore) {System.out.print("Errore");}
		JList tempo = new JList(apn_list.toArray());
		return tempo;
 	}
 public static void main(String[] arg)
 {
 	new connectui();
 	
 }
 class SimpleRunner implements Runnable //this is a background-thread that run all the time during the execution
 {                                      // thanks to the infinite cycle for(;;)
 	public void run()
 	{
 		Process stacontrol;
 		for (;;){
 		try{stacontrol = Runtime.getRuntime().exec("sakis3gz status");
 				BufferedReader in = new BufferedReader(new InputStreamReader(stacontrol.getInputStream()));				
 						String returned = in.readLine();
 						if (runn)
 						{} else {status.setText(returned);}
 			}
 		catch(Exception err) {}
 	}}
 }
 }
 		
 		
 	
