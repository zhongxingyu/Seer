 /*******************************************************************************************************
 Database class
 Commands to be implemented:
 1)create db database_name -    -------------------------------------------------completed
 2)use db database_name         -------------------------------------------------completed
 3)create table table_name column1 column2 column3...----------------------------completed
 	if no database is selected by using "use" command then table 
 	is created in default database(master)
 4)insert into table_name datatype1 col1 datatype2 col2 datatype3 col3 ....------------------------------------completed
 5)select col1 col2...from table_name where col1 value------------------	()
               select * from table_name    ------------------------complete
               select col1 col2...from table_name-------------------complete
               select * from table_name where column_name=col2-------complete
               select col1 col2...from table_name where name=col2----complete
6)delete db database_name-------------------------------------------------------completed
  delete table table_name-------------------------------------------------------completed
 7)exit    ----------------------------------------------------------------------completed
 
 Added Datatypes:
 int
 double
 string
 bool
 ********************************************************************************************************/
 import java.io.*;
 //import java.nio.file.Files;
 import java.util.StringTokenizer;
 public class dbase {
 	static String currentdb="master";
 	static String row0;
 	static String row;
 	static String path;
 	static String datatype;
 	static void create(StringTokenizer command)
 	{		
 		switch(command.nextToken()){  
 		case "db":                   //create database
 			System.out.println("CREATE Database");
 			createdb(command.nextToken());
 			break;
 		case "table":              //create table
 		    path="D:\\dbase/"+currentdb+"/"+command.nextToken()+".csv";
 		    row0="";
 			row="";
 			while (command.hasMoreTokens())    //to make columns of the table 
 			{
 				datatype=command.nextToken();
 				switch(datatype){
 				case "int":
 					break;
 				case "string":
 					break;
 				case "double":
 					break;
 				case "bool":
 					break;
 				default:
 						System.out.println("Invalid Datatype to Insert");
 						return;
 				}
 				row0=row0+datatype+",";
 				row=row +command.nextToken()+",";
 			}
 			System.out.println("CREATE Table");
 			//System.out.println(row);
 			createtable(path);
 			row="";
 			break;
 		default:
 			System.out.println("create : Invalid Command");
 		}
 	}
 	 static void createdb(String dbname)
 	{
 		 
 		//Create a folder(Database name) inside D:\dbase
 		File db = new File("D:\\dbase/"+dbname);
         db.mkdir();
         System.out.println("Database Created :"+dbname);
 	}
 	
 	static void createtable(String path)
 	{
 		File table = new File(path);
 		try {
 			table.createNewFile();
 			File file = new File(path);
 			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 			writer.write(row0);
 			writer.newLine();
 			writer.write(row);
 			writer.newLine();
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	static void use(StringTokenizer command)
 	{
 		switch(command.nextToken()){
 		case "db":
 			currentdb=command.nextToken();
 			File dir=new File("D:\\dbase/"+currentdb);
 			if(dir.exists())
 			{
 				System.out.println("Database Selected:"+currentdb);
 			}
 			else
 			{
 				System.out.println("Database doesnot exists:"+currentdb);
 			}	
 			break;
 		default:
 			System.out.println("use : Invalid Command");
 		}
 	}
 	static void insert(StringTokenizer command)
 	{
 		switch(command.nextToken()){
 		case "into":
 			path="D:\\dbase/"+currentdb+"/"+command.nextToken()+".csv";
 			File typechk=new File(path);
 			String typecolumn[]=new String[10];
 			int countcol=0;
 			String temp ="";
 			if(typechk.exists())
 			{
 				
 				try {
 					//typechk.createNewFile();
 					BufferedReader reader = new BufferedReader(new FileReader(typechk));
 					row=reader.readLine();
 					StringTokenizer data = new StringTokenizer(row,",");
 					do
 					{
 						
 						temp=data.nextToken();
 						typecolumn[countcol]=temp;
 						//dbcolumn[count]=temp;
 						//System.out.println(index[count1]);
 						//System.out.println(temp);
 						//System.out.println(countcol+":"+temp);
 					    countcol++;	
 					}while(data.hasMoreTokens());
 					//System.out.println(countcol);
 					
 				} catch (IOException e) {
 					//e.printStackTrace();
 				}
 			}
 			else
 			{
 				System.out.println("Table Not Found");
 			}	
 			File insert=new File(path);
 			row="";
 			//System.out.println(command.countTokens());
 			if(countcol==command.countTokens())
 			{
 				int countck=0;                     
 				while (command.hasMoreTokens())    //to make columns of the table 
 				{
 					temp=command.nextToken();
 					try
 					{
 						switch(typecolumn[countck]) 
 						{
 						case "int":
 							Integer.parseInt(temp);
 							break;
 						case "string":
 							break;
 						case "double":
 							Double.parseDouble(temp);
 							break;
 						case "bool":
 							if(Boolean.parseBoolean(temp))
 							{
 								temp="true";
 							}
 							else
 							{
 								temp="false";
 							}
 							break;
 						default:
 						}
 					}catch(Exception e)
 					{
 						System.out.println("Invalid data Entry");
 						return;
 					}
 					row=row +temp+",";
 					countck++;
 				}
 			}
 			else
 			{
 				System.out.println("Insufficient/Extra Inputs");
 				return;
 			}
 			if(insert.exists())
 			{
 				
 				try {
 					//insert.createNewFile();
 					BufferedWriter writer = new BufferedWriter(new FileWriter(insert,true));
 					writer.write(row);
 					writer.newLine();
 					writer.close();
 					System.out.println("Inserted to Table");
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			else
 			{
 				System.out.println("Table Not Found");
 			}	
 			break;
 		default:
 			System.out.println("Invalid Insert Command ");
 		}
 	}
 	static void select(StringTokenizer command)
 	{
 		String dbcolumn[]=new String[10];
 		String column[]=new String[10];   //storing element of columns
 		String typecolumn[]=new String[10];
 		boolean index[]=new boolean[10]; //storing index for columns
 		String clmns;     //for storing column name
 		String clmn="";
 		String cmpr=""; //for storing element to be compare
 		boolean flag=false; //to indicate where exists or not
 		int colno=0;   //for storing which column to compare
 		int count=0;
 		String temp="";
 		temp=command.nextToken();
 		while(!temp.equals("from"))
 		{
 			column[count]=temp;
 			//System.out.println(column[count]);
 			temp=command.nextToken();
 		    count++;	
 		}
 		//System.out.println("count :"+count);
 		path="D:\\dbase/"+currentdb+"/"+command.nextToken()+".csv";
 		//System.out.println(path);
 		File selct=new File(path);
 		if(command.hasMoreTokens())
 		{
 		if(command.nextToken().equals("where"))
 		{
 			flag=true;
 			clmns=command.nextToken();
 			StringTokenizer data1 = new StringTokenizer(clmns,"=");
 			clmn=data1.nextToken();
 			//System.out.println(clmn);
 			cmpr=data1.nextToken();
 			//System.out.println(cmpr);
 		}
 		}
 		if(selct.exists())
 		{
 			try {
 				//selct.createNewFile();
 				int countcol=0;
 				BufferedReader reader = new BufferedReader(new FileReader(selct));
 				row0=reader.readLine();
 				row=reader.readLine();
 				StringTokenizer data2 = new StringTokenizer(row0,",");
 				do
 				{
 					
 					temp=data2.nextToken();
 					typecolumn[countcol]=temp;
 					//dbcolumn[count]=temp;
 					//System.out.println(index[count1]);
 					//System.out.println(temp);
 					//System.out.println(countcol+":"+temp);
 				    countcol++;	
 				}while(data2.hasMoreTokens());
 				StringTokenizer data = new StringTokenizer(row,","); // To read input command word by word
 				//System.out.println(column[0]);
 				if(column[0].equals("*"))
 				{
 					StringTokenizer datarow = new StringTokenizer(row,",");
 					//System.out.println(row);
 					//row=reader.readLine();
 					int count2=0;
 					int colexist=0;
 					do
 					{
 						//dbcolumn[count]=temp;
 						//System.out.println(index[count1]);
 						//System.out.println(temp);
 						temp=datarow.nextToken();
 						if (flag)
 						{
 							if(temp.equals(clmn))
 							{
 								colexist=1;
 								colno=count2;
 								//System.out.println(colno);
 							}
 						}
 						System.out.print(temp+"\t");
 					    count2++;	
 					}while(datarow.hasMoreTokens());
					if(colexist==0)
 					{
 						System.out.println();
 						System.out.println("Invalid Column name :"+clmn);
 						return;
 					}
 					//System.out.println(cmpr);
 					try
 					{
 						//System.out.println(cmpr);
 						//System.out.println(colno+":"+typecolumn[colno]);
 						switch(typecolumn[colno]) 
 						{
 						case "int":
 							Integer.parseInt(cmpr);
 							break;
 						case "string":
 							break;
 						case "double":
 							Double.parseDouble(cmpr);
 							break;
 						case "bool":
 							if(Boolean.parseBoolean(cmpr))
 							{
 								temp="true";
 							}
 							else
 							{
 								temp="false";
 							}
 							break;
 						default:
 							System.out.println("default");
 						}
 					}catch(Exception e)
 					{
 						System.out.println();
 						System.out.println("Invalid datatype");
 						return;
 					}
 					System.out.println();
 					row=reader.readLine();
 					//System.out.println(row);
 					//StringTokenizer data2 = new StringTokenizer(row,",");
 					//System.out.println(data.countTokens());
 					while((row!=null))
 					{
 						StringTokenizer data3 = new StringTokenizer(row,",");
 						count2=0;
 						do
 						{
 							temp=data3.nextToken();
 							column[count2]=temp;
 							//System.out.println(count2+":"+column[count2]);
 						    count2++;	
 						}while(data3.hasMoreTokens());
 						//System.out.println(flag);
 						if(flag)
 						{
 							//System.out.println(cmpr);
 							//System.out.println(column[colno]);
 							if(column[colno].equals(cmpr))
 							{
 								for(int i=0;i<count2;i++)
 								{
 									System.out.print(column[i]+"\t");
 								}
 								System.out.println();
 							}
 						}
 						else
 						{
 							for(int i=0;i<count2;i++)
 							{
 								System.out.print(column[i]+"\t");
 							}
 							System.out.println();
 						}
 						row=reader.readLine();
 						//System.out.println(row);
 						
 					}
 				}
 				else
 				{
 				int count1=0;
 				int colexist=0;
 				do
 				{
 					temp=data.nextToken();
 					dbcolumn[count1]=temp;
 					//System.out.print(temp+"\t");
 					if (flag)
 					{
 						if(temp.equals(clmn))
 						{
 							colexist=1;
 							colno=count1;
 							//System.out.println("column no :"+colno);
 						}
 					}
 					//temp=data.nextToken();
 					count1++;
 				}while(data.hasMoreTokens());
				if(colexist==0)
 				{
 					System.out.println();
 					System.out.println("Invalid Column name :"+clmn);
 					return;
 				}
 				//System.out.println(cmpr);
 				try
 				{
 					//System.out.println(cmpr);
 					//System.out.println(colno+":"+typecolumn[colno]);
 					switch(typecolumn[colno]) 
 					{
 					case "int":
 						Integer.parseInt(cmpr);
 						break;
 					case "string":
 						break;
 					case "double":
 						Double.parseDouble(cmpr);
 						break;
 					case "bool":
 						if(Boolean.parseBoolean(cmpr))
 						{
 							temp="true";
 						}
 						else
 						{
 							temp="false";
 						}
 						break;
 					default:
 						System.out.println("default");
 					}
 				}catch(Exception e)
 				{
 					//System.out.println();
 					System.out.println("Invalid datatype");
 					return;
 				}
 				//System.out.println();
 				//System.out.println("Count1:"+count1);
 				for(int j=0;j<count1;j++)
 				{
 					index[j]=false;
 					//System.out.println(j+":"+index[j]);
 				}
 				for(int i=0;i<count1;i++)
 				{
 					for(int j=0;j<count;j++)
 					{
 						//System.out.println(temp+","+column[i]);
 						if(dbcolumn[i].equals(column[j]))
 						{
 							index[i]=true;
 							//System.out.print(count1+" "+index[count1]);
 							System.out.print(dbcolumn[i]+"\t");
 						}
 					}
 				    //count1++;	
 				}
 				///for(int j=0;j<count1;j++)
 				//{
 				//	System.out.println(j+":"+index[j]);
 				//}
 				System.out.println();
 				row=reader.readLine();
 				//System.out.println(row);
 				while(row!=null)
 				{
 					StringTokenizer datarow = new StringTokenizer(row,","); // To read input command word by word
 					//temp=datarow.nextToken();
 					int count2=0;
 					do
 					{
 						//dbcolumn[count]=temp;
 						//System.out.println(index[count1]);
 						//System.out.println(temp);
 						temp=datarow.nextToken();
 						column[count2]=temp;
 						//System.out.println(count2+":"+column[count2]);
 					    count2++;	
 					}while(datarow.hasMoreTokens());
 					//System.out.println(row);
 					if(flag)
 					{
 						//System.out.println(cmpr);
 						//System.out.println(column[colno]);
 						if(column[colno].equals(cmpr))
 						{
 							for(int i=0;i<count2;i++)
 							{
 								if (index[i])
 								{
 								System.out.print(column[i]+"\t");
 								}
 							}
 							System.out.println();
 						}
 					}
 					else
 					{
 						//System.out.println("ecount2 :"+count2);
 						for(int i=0;i<count2;i++)
 						{
 							if (index[i])
 							{
 							System.out.print(column[i]+"\t");
 							}
 						}
 						System.out.println();
 					}
 					//System.out.println();
 					row=reader.readLine();
 				}
 				reader.close();
 				//System.out.println("Inserted to Table");
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else
 		{
 			System.out.println("Table Not Found");
 		}	
 	}
	static void delete(StringTokenizer command)
 	{
 		switch(command.nextToken()){  
 		case "db":                   //create database
 			System.out.println("Delete Database");
 			String dbname=command.nextToken();
 			String path="D:\\dbase/"+dbname;
 			File db = new File(path);
 			System.out.println(path);
 			if(dbname.equals("master"))
 			{
 				System.out.println("Cannot Delete Master");
 			}
 			else if(deletedb(db))
 	        {
 	        System.out.println("Database Deleted :"+dbname);
 	        }
 	        else
 	        {
 	        	System.out.println("Database Delete failed :"+dbname);
 	        }
 			break;
 		case "table":              //create table
 			System.out.println("Delete Table");
 		    path="D:\\dbase/"+currentdb+"/"+command.nextToken()+".csv";
 		    File table = new File(path);
 		    if(table.delete())
 	        {
	        System.out.print("Table Deleted");
 	        }
 	        else
 	        {
 	        	System.out.print("Table Deletionfailed");
 	        }
 			break;
 		default:
 			System.out.println("Delete : Invalid Command");
 		}
 	}
 	static void describe(StringTokenizer command)
 	{
 		path="D:\\dbase/"+currentdb+"/"+command.nextToken()+".csv";
 		File typechk=new File(path);
 		String typecolumn[]=new String[10];
 		String dbcolumn[]=new String[10];
 		int countcol=0;
 		String temp ="";
 		if(typechk.exists())
 		{
 			
 			try {
 				//typechk.createNewFile();
 				BufferedReader reader = new BufferedReader(new FileReader(typechk));
 				row0=reader.readLine();
 				row=reader.readLine();
 				StringTokenizer data = new StringTokenizer(row0,",");
 				do
 				{
 					
 					temp=data.nextToken();
 					typecolumn[countcol]=temp;
 					//dbcolumn[count]=temp;
 					//System.out.println(index[count1]);
 					//System.out.println(temp);
 					//System.out.println(countcol+":"+temp);
 				    countcol++;	
 				}while(data.hasMoreTokens());
 				countcol=0;
 				//System.out.println(countcol);
 				StringTokenizer data1 = new StringTokenizer(row,",");
 				do
 				{
 					
 					temp=data1.nextToken();
 					dbcolumn[countcol]=temp;
 					//dbcolumn[count]=temp;
 					//System.out.println(index[count1]);
 					//System.out.println(temp);
 					//System.out.println(countcol+":"+temp);
 				    countcol++;	
 				}while(data1.hasMoreTokens());
 				for(int i=0;i<countcol;i++)
 				{
 					System.out.println(dbcolumn[i]+"\t"+typecolumn[i]);
 				}
 			} catch (IOException e) {
 				//e.printStackTrace();
 			}
 		}
 		else
 		{
 			System.out.println("Table Not Found");
 		}	
 	}
 	static boolean deletedb(File dir) {
 	    if (dir.isDirectory()) {
 	        String[] children = dir.list();
 	        for (int i=0; i<children.length; i++) {
 	            boolean success = deletedb(new File(dir, children[i]));
 	            if (!success) {
 	                return false;
 	            }
 	        }
 	    }
 
 	    // The directory is now empty so delete it
 	    return dir.delete();
 	}
 	
 	public static void main (String[] argv) throws IOException
 	{
 		boolean inf=true;
 		do
 		{
 		String input; //For storing command string
 		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 		input=in.readLine();
 		try
 		{
 		StringTokenizer command = new StringTokenizer(input," "); // To read input command word by word
 		String title = command.nextToken(); //First word in command string
 		switch(title){
 		case "create":                    //create command
 			create(command);
 			break;
 		case "use":                             //use command
 			//title2=command.nextToken();
 			use(command);
 			break;
 		case "insert":
 			insert(command);
 			break;
 		case "select":
 			select(command);
 			break;
		case "delete":
			delete(command);
 			break;
 		case "describe":
 			describe(command);
 			break;
 		case "exit":
 			inf=false;
 			break;
 		default:
 			System.out.println("Invalid Command");
 		}
 		}catch(Exception e){
 			System.out.println("Invalid command or Unable to retrive");
 		}
 		
 	  }while(inf);
 	}
 
 }
