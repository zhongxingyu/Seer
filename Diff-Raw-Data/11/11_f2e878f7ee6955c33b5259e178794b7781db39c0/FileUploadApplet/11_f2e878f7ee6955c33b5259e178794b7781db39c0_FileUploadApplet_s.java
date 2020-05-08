 package asp.labs;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import javax.swing.JApplet;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 
 /* 
  <OBJECT classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93"  
  width=150 height=100  
  codebase="http://java.sun.com/products/plugin/1.3/jinstall-13-win32.cab#Version=1,3,0,0">  
  <PARAM NAME="code" value="FileUploadApplet.class"> 
  </OBJECT> 
 
  Sign it!
  jarsigner fileupload.jar -keystore ~/Dev/java/FileUploadApplet/src/main/keystore/signing-jar.keystore applet
  */
 public class FileUploadApplet extends JApplet {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3936002893065666228L;
 	private static final String Boundary = "--7d021a37605f0";
 
 	public static void upload(File f) throws Exception {
 		try {
 			MultipartUtility multipart = new MultipartUtility("http://vimeotest-asp.dotcloud.com/upload", "UTF-8", "dev", "dev");
 			multipart.addFormField("absolute_path", f.getAbsolutePath());
 			multipart.addFilePart("userfile", f);
 
 			List<String> response = multipart.finish();
 			System.out.println("SERVER REPLIED:");
 			for (String line : response) {
 				System.out.println(line);
 			}
 		} catch (IOException ex) {
 			System.out.println("ERROR: " + ex.getMessage());
 			ex.printStackTrace();
 		}
 	}
 
 	@Override
 	public void init() {
 		JButton b = new JButton("File");
 		b.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				JFileChooser fc = new JFileChooser();
 				fc.setCurrentDirectory(new File("C:\\"));
 				int returnVal = fc.showSaveDialog(FileUploadApplet.this);
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					File f = fc.getSelectedFile();
 					System.out.println(f.getAbsolutePath());
 					System.out.println(f.getName());
 					try {
 						FileUploadApplet.upload(f);
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 
 			}
 		});
 		getContentPane().add(b);
 	}
 }
