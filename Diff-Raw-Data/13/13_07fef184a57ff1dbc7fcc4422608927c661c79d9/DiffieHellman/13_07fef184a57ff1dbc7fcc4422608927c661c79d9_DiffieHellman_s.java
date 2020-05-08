 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package eccdh.algorithm;
 
 /**
  *
  * @author rolf
  */
 public abstract class DiffieHellman {
 
     public abstract String execute(Configuration config) throws EncryptionException;
         //Key Agreement Schemes: S 45.
         //Key derivation function KDF: S 29
         //Hash Functions for Key derivation function: S 28
         //Diffie-Hellman Privitives: S 25
        
         
         
         //Algorithmus siehe Key Agreement Schemes S 45ff: 
         //0. einigung auf algorithmus: einigung auf key derivation function KDF + Hashfunktion 
         //      sowie ob Diffie-Hellman oder cofactor-Diffie-Hellman verwendet werden soll
         //1. finde gemeinsame Kurve und gemeinsamen Punkt auf der Kurve
         //2. berechne Private Key d und Public Key Q
         //3. Tausche die Public Keys aus
         //4. Wenn standardverfahren verwendet wird: überprüfe, ob bekommener Public Key valid ist. 
         //5. benutze Diffie Hellman welcher in Punkt 0 ausgewählt wurde, um das shared secret field z zu generieren. Siehe S 25ff. 
         //5a. Normaler Diffie Hellman: 
         //5b. berechne Punkt P = d*Q, wobei d der eigene private key ist und Q der des Verschlüsselungspartners. 
         //5c. überprüfe, ob P != 0. Wenn P==0 dann gib invalid aus
         //5d. gib z=P.x als das shared secret field aus. 
         //6. konvertiere z in einen octet-string
         //7. Benutze die in Punkt 0 ausgewählte Key derivation function KDF um Keyring data zu generieren. Siehe S 29ff. 
         //      Falls die Funktion invalid zurückgibt, invalid zurückgeben.
         //7a. Ansi Key Derivation Function: 
         //7b. überprüfe, ob z.length + sharedInfo.length + 4 < hashmaxlen. Wenn es größer ist, gib invalid aus
         //7c. überprüfe, ob Keydatalen < hashlen * (2^32 - 1). Wenn es größer ist, gibt invalid aus 
         //7d. iniziiere einen 4 octets langen big-endian octet string als counter=00000001
         //7e. for i = 1 to Math.ceil( keydatalen/hashlen )
         //7f.       compute ki = Hash(z||counter||sharedInfo) using the selected hash function from the list of approved hash functions siehe S 28
         //7g.       counter++
         //7h.       i++
         //7i. k = k1||k2||...||klast
         //return K
 }
