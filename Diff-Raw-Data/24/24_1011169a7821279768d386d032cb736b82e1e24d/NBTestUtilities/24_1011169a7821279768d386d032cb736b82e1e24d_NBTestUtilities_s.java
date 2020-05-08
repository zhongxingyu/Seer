 /*
  * Thibaut Colar Mar 10, 2010
  */
 package net.colar.netbeans.fan.test;
 
 import java.io.File;
import java.io.InputStream;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
import net.jot.prefs.JOTPropertiesPreferences;
 import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.GsfUtilities;
 import org.netbeans.modules.parsing.api.Snapshot;
 import org.netbeans.modules.parsing.api.Source;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 
 /**
  *
  * @author thibautc
  */
 public class NBTestUtilities
 {
 
 	public static Snapshot textToSnapshot(String text, String mimeType) throws Exception
 	{
 		BaseDocument doc = new BaseDocument(true, mimeType);
 		StringReader reader=new StringReader(text);
 		doc.read(reader, 0);
 		Source source = Source.create(doc);
 		Snapshot snap = source.createSnapshot();
 		return snap;
 	}
 
	public static Snapshot fileToSnapshot(File f)
 	{
 		FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
		BaseDocument doc = GsfUtilities.getDocument(fo, true);
		Source source = Source.create(doc);
 		Snapshot snap = source.createSnapshot();
		return snap;
 	}
 
 	public static List<File> listAllFanFilesUnder(String folderPath) throws Exception
 	{
 		List<File> results = new ArrayList<File>();
 		File folder = new File(folderPath);
 		File[] files = folder.listFiles();
 		for (File f : files)
 		{
 			if (f.isDirectory())
 			{
 				results.addAll(listAllFanFilesUnder(f.getAbsolutePath()));
 			} else
 			{
 				if (f.getName().equals("gamma.fan")) // Known invalid file
 				{
 					continue;
 				}
 				if (f.getName().endsWith(".fan") || f.getName().endsWith(".fwt"))
 				{
 					results.add(f);
 				}
 			}
 		}
 		return results;
 	}
 
 }
