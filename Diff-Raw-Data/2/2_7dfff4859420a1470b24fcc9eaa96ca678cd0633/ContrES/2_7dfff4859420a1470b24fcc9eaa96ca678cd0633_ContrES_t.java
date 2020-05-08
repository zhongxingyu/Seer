 package Modules;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.Arrays;
 import java.util.List;
 
 import resources.Modules.ContrESHelper;
 import ru.sabstest.Log;
 import ru.sabstest.Pack;
 import ru.sabstest.Settings;
 
 public class ContrES extends ContrESHelper {
 
     public void testMain(Object[] args) {
 	List<String> st = Arrays.asList((String[]) args[0]);
 	String num = (String) args[1];
 
 	if (st.contains("CopyToSABS"))
 	    Pack.copyToSABS(num);
 
 	callScript("SABS.VFD", new String[] { Settings.Login.contres.key });
 	callScript("SABS.StartSABS", new String[] {
 		Settings.Login.contres.user, Settings.Login.contres.pwd,
 		Settings.Login.contres.sign });
 
 	if (st.contains("VERnach") || st.contains("VERotv")) {
 	    Menutree().click(atName("  "));
 
 	    callScript("SABS.VFD", new String[] { Settings.Login.contresGUkey });
 
 	    run(Settings.path + "\\bin\\clienXML.exe -ipv1 "
 		    + Settings.Login.contresGUprofile + " 0", Settings.path
 		    + "\\bin");
 
 	    sleep(1);
 	    OKGUbutton().click();
 
 	    // profilecomboBox().click(ARROW);
 	    // profilecomboBox().click(atText(Settings.Login.contresGUprofile));
 	    // okbutton().click();
 	    if (st.contains("VERnach")) {
 		ESpanel().click(atPoint(45, 15));
 		while (Errorwindow().exists())
 		    OKerrorbutton().click();
 	    }
 
 	    if (st.contains("VERotv")) {
 		ESpanel().click(atPoint(65, 15));
 		while (Errorwindow().exists())
 		    OKerrorbutton().click();
 	    }
 	    sleep(2.0);
 	    Menutree().click(atName(" "));
 	}
 
 	if (st.contains("UFEBSnach") || st.contains("UFEBSotv")) {
 	    Menutree().click(atName("  ()"));
 
 	    if (st.contains("UFEBSnach")) {
 		ESpanel().click(atPoint(45, 15));
 		while (Errorwindow().exists())
 		    OKerrorbutton().click();
 	    }
 
 	    if (st.contains("UFEBSotv")) {
 		ESpanel().click(atPoint(65, 15));
 		while (Errorwindow().exists())
 		    OKerrorbutton().click();
 	    }
 	    sleep(2.0);
 	    Menutree().click(atName(" "));
 	}
 
 	Log.msg(" .");
 
 	callScript("SABS.CloseSABS");
 
 	if (st.contains("VERnach"))
 	    copyFromSABS(num, true);
 	else if (st.contains("UFEBSnach"))
 	    copyFromSABS(num, false);
     }
 
     private void copyFromSABS(String num, boolean isVER) {
 	File[] files;
 	if (isVER)
 	    files = new File(Settings.path + "post\\kPuO\\").listFiles();
 	else
 	    files = new File(Settings.path + "post\\kUfO\\").listFiles();
 
 	run(Settings.path + "\\bin\\ConvXML.exe", Settings.path + "\\bin");
 
 	for (File fl : files) {
 	    String key, profile;
 
 	    if (isVER) {
 		key = Settings.Sign.keycontr;
 		profile = Settings.Sign.signcontr;
 	    } else {
 		key = Settings.Login.contres.key;
		profile = Settings.Login.contres.sign;
 	    }
 	    callScript("SABS.VFD", new String[] { key });
 	    sleep(1);
 
 	    run(Settings.path + "\\bin\\clienXML.exe -ipv0 " + profile + " 0",
 		    Settings.path + "\\bin");
 	    sleep(1);
 
 	    run(Settings.path + "\\bin\\clienXML.exe -xtf "
 		    + fl.getAbsolutePath() + " 1 1", Settings.path + "\\bin");
 	    sleep(1);
 
 	    File deFile = new File(fl.getParentFile(), "XmlFileKA.dat");
 
 	    try {
 		BufferedReader br = new BufferedReader(new FileReader(deFile));
 
 		String xml = br.readLine();
 		br.close();
 
 		int signIndex = xml.lastIndexOf("o000000");
 
 		xml = xml.substring(0, signIndex);
 
 		File nf = new File(Settings.fullfolder + "\\output\\" + num
 			+ "\\" + fl.getName());
 
 		if (nf.exists())
 		    nf.delete();
 
 		BufferedWriter bw = new BufferedWriter(new FileWriter(nf));
 
 		bw.write(xml);
 		bw.flush();
 		bw.close();
 
 	    } catch (Exception e) {
 
 		e.printStackTrace();
 	    }
 
 	    // Pack.copyFile(deFile.getAbsolutePath(), Settings.fullfolder
 	    // + "\\output\\" + num + "\\" + fl.getName());
 
 	    deFile.delete();
 	    deFile = new File(fl.getParentFile(), "XmlFileZK.dat");
 	    if (deFile.exists())
 		deFile.delete();
 	}
 
     }
 }
