 import java.io.*;
 import java.nio.ByteBuffer;
 import java.util.Date;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Map;
 import java.util.HashMap;
 
 class FlattenAllProcedures {
     public static void main(String[] args) {
       try
       { 
         BufferedReader in = new BufferedReader(new FileReader("/Users/blahiri/healthcare/data/cloudera_challenge/patients.csv"));
         //Map<Integer, String> allPatients = new HashMap<Integer, String>();
         RandomAccessFile allPatients = new RandomAccessFile("/Users/blahiri/healthcare/data/cloudera_challenge/patients_random_access", "rw");
         int loopc = 0;
         DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 
        while (in.ready() 
               //&& loopc <= 20
        ) { 
            String line = in.readLine();
            int firstComma = line.indexOf(',');
            Integer patientId = Integer.parseInt(line.substring(0, firstComma));
           String recordForPatient = line.substring(firstComma, line.length());
            loopc++;
            allPatients.seek(patientId.longValue());
            //allPatients.write(ByteBuffer.wrap(recordForPatient.getBytes()));
            allPatients.writeBytes(recordForPatient);
           if (loopc % 1000 == 0)
            {
              Date date = new Date();
              System.out.println("loopc = " + loopc + ", time = " + dateFormat.format(date));
            }
        }
        byte[] b = new byte[50];
       long lPatientId = 3_4485_1495L;
       System.out.println("looking up = " + lPatientId);
        allPatients.seek(lPatientId);
       String s = allPatients.readUTF();
       System.out.println(s);
        allPatients.close();
       }
       catch(Exception e)
       {
         e.printStackTrace();
       } 
     }
 }
 
