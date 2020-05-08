 package org.alder.fotobuchconvert.ifolor;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.alder.fotobuchconvert.ifolorconvert.TestData;
 import org.alder.fotobuchconvert.ifolorencryption.Decryptor;
 
 public class DecryptMain {
 	// private static final String CHARSET_ISO_8859_1 = "ISO_8859_1";
 	private static final String CHARSET_cp1252 = "Windows-1252";
 
 	public static void main(String[] args) throws IOException {
 		Decryptor decryptor = new Decryptor();
		File projectFile = new File(TestData.getTestProject() + ".dpp");
 		byte[] bytes = decryptor.loadBinaryFile(projectFile, "DPP");
 		System.out.print(new String(bytes, 0, bytes.length, CHARSET_cp1252));
 	}
 }
