import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import javax.swing.JTextArea;
 
 import gnu.io.SerialPortEvent;
 import gnu.io.SerialPortEventListener;
 
 
 public class SerialReader implements SerialPortEventListener {
 	private InputStream input;
 	private JTextArea info;
 	private BufferedOutputStream outputStreamGyro;
 	private BufferedOutputStream outputStreamAcce;
 	private PrintWriter writerGyro;
 	private PrintWriter writerAcce;
 	
 	private final Object lock = new Integer(0);
 	
 
 	public SerialReader(InputStream in, JTextArea info) {
 		input = in;
 		this.info = info;
 		try {
 			outputStreamGyro = new BufferedOutputStream(new FileOutputStream( new File("giro.txt") ) );
 			outputStreamAcce = new BufferedOutputStream(new FileOutputStream( new File("accele.txt") ) );
 			writerGyro = new PrintWriter(outputStreamGyro);
 			writerAcce = new PrintWriter(outputStreamAcce);
 			//outputStreamGyro = new BufferedOutputStream(new FileOutputStream( new File("giro.txt") ) );
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		setSocket();
 	}
 	
 	
 	
 	OutputStream os;
 	ServerSocket ss;
 	private void setSocket() {
 		
 		try {
 			ss = new ServerSocket(2345);
 			//Socket s = ss.accept();
 	        //os = s.getOutputStream();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         
 	}
 
 
 
 
 	public void chiudi(){
 		chiudi=true;
 	}
 	
 
 	String buffer = "";
 	byte tmp[] = new byte[1024];
 	
 	boolean alto = false;
 	int conta = 0, soglia = 50;
 	long time = System.currentTimeMillis(), totalOuts=0;
 	int attuale=0;
 	int val;
 	private boolean chiudi=false;
 	private byte tipoSensore;
 	
 	private PrintWriter selezionato;
 	
 	@Override
 	public void serialEvent(SerialPortEvent arg0) {
 		synchronized (lock) {
 
 		try {
 			
 			if( input.available() > 0 ){
 				int len = input.read(tmp);
 				//buffer = buffer + new String(tmp, 0, len);
 				/*
 				if (outputStream!=null){					
 					outputStream.write(tmp, 0, len);
 				}
 				*/
 				/* roba di debug */
 				for (int i = 0;i < len; i++){
 					switch(attuale){
 						case 0:
 							tipoSensore = tmp[i];
 							switch (tipoSensore) {
 								case 'G':
 									selezionato = writerGyro;
 									System.out.print("Giro: ");
 									break;
 								case 'A':
 									selezionato = writerAcce;
 									System.out.print("Accel: ");
 									break;
 								default:
 									selezionato = null;
 									System.out.print("BHO!: ");
 									break;
 							}
 							attuale++;
 							break;
 						case 1:// MS x
 							readMSbyte(i);
 							attuale++;
 							break;
 						case 2:// LS x
 							readLAbyte(i);
 							System.out.print(" x:"+val);
 							selezionato.print(val+" ");
 							attuale++;
 							break;
 						case 3:// MS y
 							readMSbyte(i);
 							attuale++;
 							break;
 						case 4:// LS y
 							readLAbyte(i);
 							System.out.print(" y:"+val);
 							selezionato.print(val+" ");
 							attuale++;
 							break;
 						case 5://MS z
 							readMSbyte(i);
 							attuale++;
 							break;
 						case 6://MS z
 							readLAbyte(i);
 							System.out.print(" z:"+val);
 							selezionato.print(val+"\n");
 							System.out.println(); //vai pure a capo
 							attuale=0;
 							break;
 					}
 					/*
 					//se la conversione è attiva
 					if (attuale > 0){
 						//se, fin dall'inizio delle stream, è un byte di indice pari (partendo però da 1), allora è il byte meno significativo dell'int
 						if (attuale%2==0){
 							//quindi il più significativo, che sarà già stato letto, spostalo a sinistra
 							val = val << 8;
 							//copia in val i byte ad uno del byte meno significativo, completando così il dato (or logico, per evitare casini con i signed/unsigned se avessi usato +)
 							val |= tmp[i];
 							//stampa il dato a video							
 							System.out.print(val+" "); 
 						}else{	
 							//altrimenti questo è il primo byte dell'int
 							//azzera il valore precendente
 							val = 0; 
 							//e metti gli 1 di tmp in val (or logico, per evitare casini con i signed/unsigned se avessi usato +)
 							val |= tmp[i]; 
 						}
 						//sposta avati di uno l'indice globale di steam
 						attuale++; 
 						//se abbiamo letto un'intera sequenza x, y, z (6byte)
 						if (attuale == 7){
 							//sappiamo che lo stream si ripeterà, quindi mettilo ad 1.
 							attuale=1; 
 							//vai a capo, lasciando quindi una tripletta x,y,z in ogni riga
 							System.out.println();  
 						}
 					}
 					*/
 					totalOuts++;
 					if (time +1000 <= System.currentTimeMillis()){
 						System.out.println("letture al secondo:"+totalOuts);
 						totalOuts=0;
 						time = System.currentTimeMillis();
 					}
 				}
 			}
 		} catch (IOException e) {
 			info.append("\nErrore nella porta seriale: "+e);
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			chiudiFile();
 		}
 		
 		if (chiudi){
 			try {
 				chiudiFile();
 				input.close();
 				System.out.println("flusso da seriale chiuso, ma mancano: "+input.available());
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		}
 	}
 
 
 
 	private void readLAbyte(int i) {
 		//quindi il più significativo, che sarà già stato letto, spostalo a sinistra
 		val = val << 8;
 		//copia in val i byte ad uno del byte meno significativo, completando così il dato (or logico, per evitare casini con i signed/unsigned se avessi usato +)
 		val |= tmp[i] & 0xff;
 	}
 
 
 
 	private void readMSbyte(int i) {
 		//azzera il valore precendente
 		val = 0;
 		//e metti il valore di tmp in val (or logico, per evitare casini con i signed/unsigned se avessi usato +)
 		val |= tmp[i];
 	}
 
 
 
 	private void chiudiFile() {
 		if (outputStreamGyro!=null){
 			try {
 				outputStreamGyro.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			outputStreamGyro= null;
 			System.out.println("GYRO: flusso su file chiuso");
 		}
 		if (outputStreamAcce!=null){
 			try {
 				outputStreamAcce.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			outputStreamAcce= null;
 			System.out.println("ACCE: flusso su file chiuso");
 		}
 		if (ss!=null){
 			try {
 				ss.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			ss = null;
 			System.out.println("Server: flusso su TCP chiuso");
 		}
 	}
 
 }
