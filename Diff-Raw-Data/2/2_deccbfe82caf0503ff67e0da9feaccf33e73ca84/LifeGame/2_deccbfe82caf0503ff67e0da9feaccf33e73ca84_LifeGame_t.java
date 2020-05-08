 import java.io.*;
 import java.util.Scanner;
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferStrategy;
 public class LifeGame extends Canvas {
 	private byte[][] cells; 
 	private long tick;
 	private int size = 50;
 	private boolean running = false;
 	private Thread t;
 	private Buttons btn;
 	private BufferStrategy bf;
 	LifeGame() {
 		bf = getBufferStrategy();
 		init();
 		setSize(1000+size,1000+size);
 		setBackground(Color.WHITE);
 		addMouseListener(new MouseListener() {
 			
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				// TODO Auto-generated method stub
 				int x = e.getX()/(getWidth()/size);
 				int y = e.getY()/(getHeight()/size);
 				System.out.println("Marking cell at: x="+x+" y="+y);
 				if(cells[x][y] == 0) {
 					cells[x][y] = 1;
 				} else {
 					cells[x][y] = 0;
 				}
 				repaint();
 			}
 		});
 		t = new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				while(true) {
 					try {
 						Thread.sleep(250);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					if(running) {
 						tick();
 						try{
 							btn.setTick();
 						} catch (NullPointerException e) {
 							// TODO: handle exception
 						}
 					}
 				}
 			}
 		});
 		t.start();
 		
 	}
 	public void paint(Graphics g) {
		createBufferStrategy(1);
 		
 		bf = getBufferStrategy();
 		g = null;
 		try{
 			g = bf.getDrawGraphics();
 			render(g);
 		} finally {
 			g.dispose();
 		}
 		bf.show();
 		Toolkit.getDefaultToolkit().sync();
 		
 	}
 	
 	private void render(Graphics g) {
 		for(int x = 0; x < cells.length; x++ ) {
 			for (int y = 0; y < cells[x].length; y++) {
 				if(cells[x][y] ==1){
 					g.setColor(Color.BLACK);
 					g.fillRect(x*(getWidth()/size), y*(getHeight()/size),getWidth()/size ,getWidth()/size);
 				//	System.out.println("Filled Square at "+x+","+y);
 				}
 				g.setColor(Color.GRAY);
 				g.drawRect(x*(getWidth()/size), y*(getHeight()/size),getWidth()/size ,getWidth()/size);
 			}
 		}
 	}
 	public void init() {
 		cells = new byte[size][size];
 		//Arrays.fill(cells,new byte[size]);
 		for (int i = 0; i < cells.length; i++) {
 			for (int j = 0; j < cells[i].length; j++) {
 				cells[i][j] = 0;
 			}
 		}
 		tick = 0;
 		try{
 			btn.setTick();
 		} catch (NullPointerException e) {
 			// TODO: handle exception
 		}
 		repaint();
 	}
 	public void importBoard() throws FileNotFoundException {
 		init();
 		Scanner in = new Scanner(new File("board.dat"));
 		while(in.hasNext()) {
 			cells[in.nextByte()][in.nextByte()] = 1;
 		}
 		in.close();
 	}
 	public long getTicks() {
 		return tick;
 	}
 	public void exportBoard() throws IOException {
 		// Save the board to a file as x y
 		BufferedWriter out = new BufferedWriter(new FileWriter("board.dat"));
 		for (int i = 0; i < cells.length; i++) {
 			for (int j = 0; j < cells[i].length; j++) {
 				if(cells[i][j] == 1) {
 					out.write(i+" "+j+" ");
 				}
 			}
 		}
 		out.close();
 	}
 	public void start(Buttons btn) {
 		// Should probably return a message to the GUI
 		this.btn = btn;
 		running = true;
 	/*	synchronized (t) {
 			t.notify();
 		}*/
 		
 	}
 	public void stop() {
 		running = false;
 		/*synchronized (t) {
 			t.notify();
 		}*/
 	}
 	private void tick() {
 		tick++;
 		byte[][] newCells = new byte[size][size];
 		int totalSum = 0;
 		// The array should wrap around so that cells[1000][1001] refers to the top right cell
 		for (int i = 0; i < cells.length; i++) {
 			for (int j = 0; j < cells[i].length; j++) {
 				
 				int ip = (((i+1)%cells.length)+cells.length)%cells.length;
 				int im = (((i-1)%cells.length)+cells.length)%cells.length;
 				int jp = (((j+1)%cells[i].length)+cells[i].length)%cells[i].length;
 				int jm = (((j-1)%cells[i].length)+cells[i].length)%cells[i].length;
 				
 				byte[] neighbours = {
 					cells[im][j],
 					cells[ip][j],
 					cells[i][jp],
 					cells[i][jm],
 					cells[ip][jp],
 					cells[ip][jm],
 					cells[im][jp],
 					cells[im][jm]
 				};
 				//System.out.println(Arrays.toString(neighbours));
 				byte sum = 0;
 				for(byte b: neighbours) {
 					sum += b;
 				}
 				totalSum += sum;
 				if(cells[i][j] == 1){
 					// Dealing with live cells
 					if(sum < 2) {
 						newCells[i][j] = 0;
 					} else if(sum > 3) {
 						newCells[i][j] = 0;
 					} else {
 						newCells[i][j] = 1;
 					}
 				} else if(sum == 3) {
 					// Dealing with dead cell
 					newCells[i][j] = 1;
 				} else {
 					newCells[i][j] = 0;
 				}
 			}
 		}
 		if(totalSum == 0) {
 			// Should probably stop now, since everyone is dead
 			System.out.println("All cells are dead, stopping!");
 			btn.stopStart.setText("Stopped");
 			btn.stopStart.setSelected(false);
 			stop();
 		}
 		cells = newCells;
 		repaint();
 	}
 }
