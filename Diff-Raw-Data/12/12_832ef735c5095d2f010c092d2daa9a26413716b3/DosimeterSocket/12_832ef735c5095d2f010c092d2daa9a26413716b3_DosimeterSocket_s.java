 package ch.cern.atlas.apvs.dosimeter.server;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import ch.cern.atlas.apvs.domain.Dosimeter;
 
 public class DosimeterSocket implements Runnable {
 
 	private Socket socket;
 	private Random random = new Random();
 	private int noOfDosimeters = 5;
 
 	public DosimeterSocket(Socket socket) {
 		this.socket = socket;
 	}
 	
 	@Override
 	protected void finalize() throws Throwable {
 		super.finalize();
 		if (socket != null) {
 			System.out.println("Closing socket");
 			socket.close();
 		}
 	}
 
 	@Override
 	public void run() {
 		try {
 			List<Dosimeter> dosimeters = new ArrayList<Dosimeter>(
 					noOfDosimeters);
 			for (int i = 0; i < noOfDosimeters; i++) {
 				dosimeters.add(new Dosimeter(random.nextInt(100000), random
						.nextInt(500), random.nextInt(5)));
 				System.out.println(dosimeters.get(i).getSerialNo());
 			}
 			
 			System.out.print("Connected on: "+socket.getInetAddress());
 
 			BufferedWriter os = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
 			while (true) {
 				for (int i = 0; i < dosimeters.size(); i++) {
 					Dosimeter dosimeter = dosimeters.get(i);
 					os.write(DosimeterCoder.encode(dosimeter));
 					os.newLine();
 					dosimeters.set(i, next(dosimeter));
 				}
 				os.flush();
 
 				try {
 					Thread.sleep(5000);
 				} catch (InterruptedException e) {
 					// ignored
 				}
 				System.out.print(".");
 				System.out.flush();
 			}
 
 		} catch (IOException e) {
 		} finally {
 			try {
 				System.out.println("Closing");
 				socket.close();
 			} catch (IOException e) {
 				// ignored
 			}
 		}
 	}
 
 	private Dosimeter next(Dosimeter dosimeter) {
 		double newDose = dosimeter.getDose() + dosimeter.getRate();
 		double newRate = Math.max(0.0,
 				dosimeter.getRate() + random.nextGaussian() * 0.5);
 		return new Dosimeter(dosimeter.getSerialNo(), newDose, newRate);
 	}
 
 }
