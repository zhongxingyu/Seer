 import java.lang.reflect.Field;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 public abstract class RPGPersisted
 {
 	protected boolean dirty = false;
 	
 	protected List<Field> fields = new ArrayList<Field>();
 	protected Field idField;
 	
 	public RPGPersisted()
 	{
 	}
 	
 	protected void onSave(PreparedStatement ps)
 	{
 		
 	}
 	
 	protected void onLoad(ResultSet rs)
 	{
 		
 	}
 	
 	protected void findFields()
 	{
 		idField = null;
 		
 		for (Field field : this.getClass().getDeclaredFields())
 		{
 			RPGPersist persist = field.getAnnotation(RPGPersist.class);
 			if (persist != null)
 			{
 				fields.add(field);
 				if (persist.id())
 				{
 					idField = field;
 				}
 			}
 		}
 	}
 	
 	public void load(Connection conn, String tableName, List<RPGPersisted> objects)
 	{
 		findFields();
 		if (fields.size() <= 0) 
 		{
 			RPG.getRPG().log(Level.WARNING, "No fields defined for persisted class: " + getClass().getName());
 			return;
 		}
 
 		PreparedStatement ps = null;
 		ResultSet rs = null;		
 		try 
         {
 			String sqlSelect = "SELECT ";
 			boolean first = true;
 			for (Field field : fields)
 			{
 				if (!first) sqlSelect += ", ";
 				first = false;
 				sqlSelect += field.getName();
 			}
 			sqlSelect += " FROM " + tableName;
 			ps = conn.prepareStatement(sqlSelect);
             rs = ps.executeQuery();
             while (rs.next()) 
             {
                 RPGPersisted newObject = (RPGPersisted)this.getClass().newInstance();
                 for (Field field : fields)
                 {
                 	field.set(newObject, rs.getObject(field.getName()));
                 }
                 newObject.onLoad(rs);
                 newObject.dirty = false;
                 objects.add(newObject);
             }     
 		}
 		catch (Exception ex) 
         {
 			RPG.getRPG().log(Level.SEVERE, "Error loading persisted class " + this.getClass().getName(), ex);
         } 
         finally 
         {
             try 
             {
                 if (ps != null) 
                 {
                     ps.close();
                 }
                 if (rs != null) 
                 {
                     rs.close();
                 }
             } 
             catch (SQLException ex) 
             {
             }
         }
 	}
 	
 	public void save(Connection conn, String tableName, List<RPGPersisted> objects)
 	{
 		findFields();
 		if (objects.size() <= 0) return;
 	
 		if (fields.size() <= 0) 
 		{
 				RPG.getRPG().log(Level.WARNING, "No fields defined for persisted class: " + this.getClass().getName());
 				return;
 		}
 		
 		for(RPGPersisted object : objects)
 		{
 			if (object.dirty)
 			{
 				PreparedStatement ps = null;
 				ResultSet rs = null;		
 				try 
 		        {
 					String fieldList = "";
 					String valueList = "";
 					String updateList = "";
 					
 					boolean first = true;
 					for (Field field : fields)
 					{
 						if (!first) 
 						{
 							fieldList += ", ";
 							valueList += ", ";
 							updateList += ", ";
 						}
 						first = false;
 						fieldList += field.getName();
 						valueList += "?";
 						updateList += field.getName() + " = ?";
 					}
 					
 					String sqlUpdate = 
 						"INSERT INTO " 
 					+ 	tableName 
 					+ 	" ("
 					+	fieldList
 					+	") VALUES ("
 					+	valueList
 					+	") ON DUPLICATE KEY UPDATE "
 					+	updateList;
 					
 					ps = conn.prepareStatement(sqlUpdate);
					int index = 0;
 					for (Field field : fields)
 	                {
						Object value = field.get(field);
						ps.setObject(index, value);
						ps.setObject(index + fields.size(), value);
 	                }
 					object.onSave(ps);
 					ps.executeUpdate();
 					object.dirty = false;
 		        }
 				catch (Exception ex) 
 		        {
 					RPG.getRPG().log(Level.SEVERE, "Error updating or inserting persisted class " + getClass().getName(), ex);
 		        } 
 		        finally 
 		        {
 		            try 
 		            {
 		                if (ps != null) 
 		                {
 		                    ps.close();
 		                }
 		                if (rs != null) 
 		                {
 		                    rs.close();
 		                }
 		            } 
 		            catch (SQLException ex) 
 		            {
 		            }
 		        }
 			}
 		}
 	}
 	
 	public boolean isDirty()
 	{
 		return dirty;
 	}
 	
 	public void setDirty(boolean dirty)
 	{
 		this.dirty = dirty;
 	}
 	
 }
