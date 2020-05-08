 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.security.KeyPair;
 
 import crypt.RSA;
 
 public class RSAKeyCreation {
 	public static void main(String[] args){
 		if(args.length == 1 && !args[0].isEmpty()){
 			System.out.println("Owner of keypair: "+args[0]);
 			String OwnerName = args[0];
 			System.out.println("Generating keys");
			KeyPair keys = RSA.generateKeyPair(1024);
 			try {
 				System.out.println("Writing public keyfile. Keyformat: "+keys.getPublic().getFormat());
 				byte[] pubKey = keys.getPublic().getEncoded();
 				File pubKeyFile = new File(OwnerName+".pub");
 				FileOutputStream pubKeyOs = new FileOutputStream(pubKeyFile);
 				pubKeyOs.write(OwnerName.length());
 				pubKeyOs.write(OwnerName.getBytes());
 				pubKeyOs.write(pubKey.length);
 				pubKeyOs.write(pubKey);
 				pubKeyOs.close();
 	
 				System.out.println("Writing private keyfile. Keyformat "+keys.getPrivate().getFormat());
 				byte[] prvKey = keys.getPrivate().getEncoded();
 				File prvKeyFile = new File(OwnerName+".prv");
 				FileOutputStream prvKeyOs = new FileOutputStream(prvKeyFile);
 				prvKeyOs.write(OwnerName.length());
 				prvKeyOs.write(OwnerName.getBytes());
 				prvKeyOs.write(prvKey.length);
 				prvKeyOs.write(pubKey);
 				prvKeyOs.close();
 				System.out.println("Keyfiles successfully created");
 			} catch (FileNotFoundException e) {
 				System.out.println("At least one of the key files could not be created");
 				//e.printStackTrace();
 			} catch (IOException e) {
 				System.out.println("An error occurred while writing to one of the key files");
 				//e.printStackTrace();
 			}
 		} else {
 			System.out.println("Please enter an owner name as first argument");
 		}
 	}
 }
