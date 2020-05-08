 package ch.almana.android.unibas.perssearch.contacts;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Environment;
 import android.widget.Toast;
 import ch.almana.android.unibas.perssearch.helper.Logger;
 import ch.almana.android.unibas.perssearch.model.Person;
 
 public class ContactAdderVcf {
 
 	public static void addContact(Context ctx, Person person) {
 		try {
 			File file = getVcfFile(ctx, person);
			Intent i = new Intent(Intent.ACTION_DEFAULT);
 			i.setDataAndType(Uri.fromFile(file), "text/x-vcard");
 			ctx.startActivity(i);
 		} catch (IOException e) {
 			Logger.w("Cannot add person to address book", e);
 			Toast.makeText(ctx, "Cannot add person to address book", Toast.LENGTH_LONG).show();
 		}
 	}
 
 	private static File getVcfFile(Context ctx, Person person) throws IOException {
 		File tmpFile = File.createTempFile("perssearch", ".vcf", Environment.getExternalStorageDirectory());
 		FileWriter fw = new FileWriter(tmpFile);
 		fw.write(person.getVcard());
 		fw.flush();
 		fw.close();
 		return tmpFile;
 	}
 
 }
