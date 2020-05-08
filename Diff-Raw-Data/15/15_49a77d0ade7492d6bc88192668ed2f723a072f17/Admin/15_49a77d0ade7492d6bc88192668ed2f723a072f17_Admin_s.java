 import java.net.*;
 import java.security.*;
 import java.security.spec.X509EncodedKeySpec;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.io.*;
 
 import org.bouncycastle.crypto.engines.RSAEngine;
 import org.bouncycastle.crypto.params.RSAKeyParameters;
 
 public class Admin {
 	
 	public static void main(String[] args){
 	
 		String url="jdbc:mysql://localhost:3306/elections";
 		String user="root";
 		String pw="";
 		try{
 			Class.forName("com.mysql.jdbc.Driver");
 		}
 		catch(Exception e){
 			System.out.println(e);
 		}
 		try{
 		    final Connection con=DriverManager.getConnection(url, user, pw);
 			final Statement st=con.createStatement();
 			System.out.println("start");
 			int servPort=33333;
 			ServerSocket servSock=new ServerSocket(servPort);	
 		
 			while(true){
 				final Socket clntSock=servSock.accept();
 				SocketAddress clientAddress=clntSock.getRemoteSocketAddress();
 				System.out.println("receiving requests from client at "+clientAddress);
 				new Thread(new Runnable() {
 					ResultSet rs=null;
 					int recvMsgSize;
 					byte [] receiveBuf=new byte[1280];
 					byte [] receiveBuf2=new byte[1280];
 					ArrayList<byte[]> bufArray = new ArrayList<byte[]>();
 				  	InputStream in=clntSock.getInputStream();
 					OutputStream out=clntSock.getOutputStream();
 					File log=new File("log.txt");
 					ByteArrayInputStream byteArray = new ByteArrayInputStream(receiveBuf2);
 					
 					
 		            java.util.Date date = new java.util.Date();
 		            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
 					PrintWriter logwrite =new PrintWriter(new BufferedWriter(new FileWriter(log, true)));  
 						public void run(){
 							try{
 						// send adminkey
 								in.read(receiveBuf);
 								String electionname=new String(receiveBuf, "UTF-8");
 								rs=st.executeQuery("SELECT pk FROM adminkeys WHERE election='"+electionname+"';");
 								rs.next();
 								byte[] adminpk=rs.getBytes("pk");
 							//receive username, blindbytes, signedBlind and electionname	
								for (int j = 0; j <= 3; j++) {
 									int tmp = byteArray.read();
 									byte[] tmpArray = new byte[tmp];
 									byteArray.read(tmpArray, 0, tmp);
 									bufArray.add(tmpArray);
 								}
 		
 			
 							    //get the value of username, blindBytes and signedBlind --- also need to get election name
 								String username = new String(bufArray.get(0), "UTF-8");
 								byte[] blindBytes=bufArray.get(1);
 								byte[] signedBlind=bufArray.get(2);
								electionname=new String(bufArray.get(3), "UTF-8");
 								logwrite.println("Time: "+sdf.format(date)+"; Event Type: Admin Receive Info; Election Name: "+electionname+"; Description: Admin received blind and signed blind from "+username+"\n");
 								//RSAPrivateKey skAdmin;
 								//System.out.println("testing2");
 				
 								//This needs to be be BC keys
 								rs=st.executeQuery("SELECT sk FROM adminkeys WHERE election='"+electionname+"';");
 								rs.next();
 								RSAKeyParameters adminsk=(RSAKeyParameters)deserialize(rs.getBytes("sk"));
 								
 								
 								//OLD STUFF
 								//byte[] adminsk=rs.getBytes("sk");
 								//skAdmin=(RSAPrivateKey)(KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(adminsk)));
 								//System.out.println("testing3");
 								//
 				
 								//get blinded message m, signature s of m, and username
 								//check that the user is eligible for this election -- database query
 								//check that user has not already asked for signature -- some storage???
 								//Check signature of the blinded message
 								//get voter pk from a database
 								//RSAPublicKey voterPK=whatever in database for that user
 								
 								
 								/*****Vote client sends voter pk to Admin****/
 								
 								rs=st.executeQuery("SELECT pk FROM voterkeys WHERE username='"+username+"'");
 								rs.next();
 								byte[] voterpk=rs.getBytes("pk");
 								
 								PublicKey pk=KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(voterpk));
 								Signature ver=Signature.getInstance("SHA256WITHRSA");
 								ver.initVerify(pk);
 								ver.update(blindBytes);
 								boolean goodSig=ver.verify(signedBlind);
 								boolean eligible=false;
 								rs=st.executeQuery("SELECT "+electionname+" FROM elections WHERE usernames='"+username+"';");
 								rs.next();
 								//System.out.println("testing4");
 								if(rs.getString(electionname).equals("1"))
 									eligible=true;
 								//Still need to check election eligibility
 								if(eligible && goodSig){
 									System.out.println("good sig");
 									RSAEngine sign=new RSAEngine();
 									sign.init(true, adminsk);
 									byte[] signed=sign.processBlock(blindBytes, 0, blindBytes.length);
 									
 									/*BigInteger n=skAdmin.getModulus();
 									BigInteger d=skAdmin.getPrivateExponent();
 									BigInteger blinded=new BigInteger(blindBytes);
 									BigInteger s=blinded.modPow(d,n);
 									byte[] signed=s.toByteArray();
 									Signature sign=Signature.getInstance("RSA");
 									sign.initSign(skAdmin);
 									sign.update(blindBytes);
 									byte[] signed=sign.sign();*/
 									out.write(signed);
 									logwrite.println("Time: "+sdf.format(date)+"; Event Type: Admin Send Info; Election: "+electionname+"; Description: Admin sent signed blind to "+username+"\n");
 								}
 								//Keep track of how many voters have requested signatures
 								//rs=st.executeQuery("SELECT numVoters FROM candidates WHERE election='"+electionname+"'");
 								/*int numVoters=Integer.parseInt(rs.getString("numVoters"));
 								count++;
 								if(numVoters==count){
 									//When everyone has voted, publish list of usernames, encrypted votes, 
 									//and signatures of encrypted votes (all data sent from voters)
 									
 								}*/
 								
 					            PreparedStatement pstmt=null;
 					            pstmt=con.prepareStatement("INSERT INTO "+electionname+" (username, encVote, signedVote) values (?,?,?);");
 					            pstmt.setString(1, username);
 					            pstmt.setBytes(2, blindBytes);
 					            pstmt.setBytes(3, signedBlind);
 					            pstmt.execute();
 								/***When all voters have received signatures from admin, send list of (username, blindBytes, signedBlind) to all voters***/
 
 					            //clntSock.close();
 					
 								logwrite.close();
 							}
 							catch (Exception e){}
 						}
 				
 				}).start();
 			}
 			
 		}
 		catch(Exception e){
 			System.out.println(e);
 		}
 	}
 	
 	private static Object deserialize(byte[] encVote) throws IOException, ClassNotFoundException {
 		ByteArrayInputStream b = new ByteArrayInputStream(encVote);
         ObjectInputStream o = new ObjectInputStream(b);
         return o.readObject();
 	}
 	
 }
