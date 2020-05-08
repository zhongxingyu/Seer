 import java.util.Date;
 import java.text.DateFormat;
 import javax.swing.JOptionPane;
 
 public class Person
 {
    String navn;
    int id;
    Sykkel sykkel;
    String merknad = "";
    Date startTid;
    static int nesteNr = 0;
 	
    public Person(String navn) {
     this.navn = navn;
     id = nesteNr++;	
    }
 
    public int getID() {
      return id;
    }
 
    public Sykkel getSykkel() { 
      return sykkel;
    }
 
    public boolean godkjent()
    {
     if (sykkel == null && merknad.equals("")) {
       return true;
     }
     return false;
    }
 
    public void setMerknad(Date t, String m) {
      DateFormat df = DateFormat.getInstance(); 
      merknad+= df.format(t) + " : " + m + "\n";
      JOptionPane.showMessageDialog(null, "F�lgende merknad er registert - " + merknad);
    }
 
    public boolean leiSykkel(Sykkel s) {
      if(godkjent()) {
       sykkel = s;
       startTid = new Date();
       return true;
      }
      return false;
    }
 
    public int leietid(Date sluttTid) {
     long varighet = (sluttTid.getTime() - startTid.getTime()); 
     int varighetTimer = (int) Math.ceil(varighet / 3600000);
 	   
     return varighetTimer;
    }
 
    public void leverInn() {
      Date innTid = new Date();
 	   
      if(leietid(innTid) > Sykkel.getMAXTID()) {
 		   
       if(leietid(innTid) - 3 == 1 ) {
         setMerknad(innTid, "Sykkel ble levert " + (leietid(innTid) - Sykkel.getMAXTID()) + " time for sent");
       }
 		   
       else {
         setMerknad(innTid, "Sykkel ble levert " + (leietid(innTid) - Sykkel.getMAXTID()) + " timer for sent");
       }
      }
     sykkel = null;
    }
 
   
 
   @Override
    public String toString() {
 	   String utskrift = navn + " ID nummer: " + id + "\n";
 	   
 	   if(sykkel != null) {
 		   utskrift += "Sykkel id: " + sykkel.getID() + "\n";
 	   }
 	   
 	   if(merknad != "") {
 		   utskrift += merknad;
 	   }
 	   
 	   return utskrift;
    }
 
 } 
