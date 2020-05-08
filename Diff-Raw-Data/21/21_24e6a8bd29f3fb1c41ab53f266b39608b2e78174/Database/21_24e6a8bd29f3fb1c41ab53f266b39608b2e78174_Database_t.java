 package game.util.DB;
 
 import game.util.DB.Fields.DBField;
 
import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 public class Database {
 
 	Hashtable<DBTable,ArrayList<DBField>> tables;
 	
 	public Database() {
 		tables = new Hashtable<>();
 	}
 	
 	synchronized public void addTable(DBTable table) {
 		tables.put(table, new ArrayList<DBField>());
 	}
 	
 	synchronized public void addField(DBTable table, DBField field) throws Exception {
 		ArrayList<DBField> fields = tables.get(table);
 		if (fields == null)
 			throw new Exception();
 		fields.add(field.create());
 	}
 	
 	synchronized public void changeField(DBTable table, DBValue[] where, Object[] values, DBField newField) throws Exception {
 		ArrayList<DBField> fields = tables.get(table);
 		if (fields == null)
 			throw new Exception();
 		if (where.length != values.length)
 			throw new Exception();
 		for (int i = 0; i < fields.size(); i++)
 			if (fields.get(i).contains(where, values))
 				fields.set(i, newField.create());
 	}
  	
 	synchronized public DBField getField(DBTable table, DBValue[] where, Object[] values) throws Exception {
 		ArrayList<DBField> fields = tables.get(table);
 		if (fields == null)
 			throw new Exception();
 		if (where.length != values.length)
 			throw new Exception();
 		for (int i = 0; i < fields.size(); i++)
 			if (fields.get(i).contains(where, values))
 				return fields.get(i).create();
 		return null;
 	}
 	
 	synchronized public void deleteField(DBTable table, DBValue[] where, Object[] values) throws Exception {
 		ArrayList<DBField> fields = tables.get(table);
 		if (fields == null)
 			throw new Exception();
 		if (where.length != values.length)
 			throw new Exception();
 		for (int i = 0; i < fields.size(); i++)
 			if (fields.get(i).contains(where, values))
 				fields.remove(i);
 	}
 
 	synchronized public void save(String fileRef) {
 		try {
			File file = new File(fileRef);
			if (file.exists())
				file.delete();
			file.createNewFile();
 			FileOutputStream fout = new FileOutputStream(fileRef);
 			ObjectOutputStream oos = new ObjectOutputStream(fout);
		    oos.writeObject(tables);
		    oos.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}

 	@SuppressWarnings("unchecked")
 	synchronized public void load(String fileRef) {
 		try {
 			FileInputStream fin = new FileInputStream(fileRef);
 		    ObjectInputStream ois = new ObjectInputStream(fin);
 		    tables = (Hashtable<DBTable,ArrayList<DBField>>) ois.readObject();
 		    ois.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
