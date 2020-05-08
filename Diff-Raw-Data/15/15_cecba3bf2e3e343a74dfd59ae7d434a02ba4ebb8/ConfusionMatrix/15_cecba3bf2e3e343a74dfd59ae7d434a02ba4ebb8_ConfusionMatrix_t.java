 /**
  * 
  */
 package spellcheck;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.Serializable;
 
 /**
  * @author abilng
  *
  */
 class  ConfusionMatrix implements Serializable {
 	
 	private static final String FILE_NAME = "ConfusionMatrix.dat";
 	private static final long serialVersionUID = 1L;
 	
 	private static final String MATRIX_TXT_PATH = "/home/abil/workspace/NLP/data/";
 	
 	private int del[][];
 	private int add[][];
 	private int sub[][];
 	private int rev[][];
 
 	public ConfusionMatrix(){
 		try {
 			read(FILE_NAME);
		} catch (Exception e) {
 			System.err.println("Data File Not Found: (" + e.getMessage() + ")");
 			System.err.println("Reading From Text File");
 			del=new int[27][27];
 			add=new int[27][27];
 			sub=new int[26][26];
 			rev=new int[26][26];
 			buildMatrices(MATRIX_TXT_PATH + "del.txt",del);
 			buildMatrices(MATRIX_TXT_PATH + "add.txt",add);
 			buildMatrices(MATRIX_TXT_PATH + "sub.txt",sub);
 			buildMatrices(MATRIX_TXT_PATH + "rev.txt",rev);
 			try {
 				save(FILE_NAME);
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 		}
 	}
 	public  void read(String file) throws IOException, ClassNotFoundException {
 		InputStream buffer;
 		buffer = new BufferedInputStream( new FileInputStream(file));
 		ObjectInput input = new ObjectInputStream ( buffer );
 		del = (int[][]) input.readObject();
 		add = (int[][]) input.readObject();
 		sub = (int[][]) input.readObject();
 		rev = (int[][]) input.readObject();
 		input.close();
 	}
 
 	public  void save(String file) throws IOException {
 		OutputStream buffer;
 		buffer = new BufferedOutputStream( new FileOutputStream(file));
 		ObjectOutput output = new ObjectOutputStream ( buffer );
 		output.writeObject(del);
 		output.writeObject(add);
 		output.writeObject(sub);
 		output.writeObject(rev);
 		output.close();
 	}
 
 
 	private static void buildMatrices(String file, int [][] arr) 
 	{
 		BufferedReader buffer;
 		try
 		{
 			buffer = new BufferedReader(new FileReader(file));   
 			String line, temp[];
 			int i=0,j;
 
 			while ((line = buffer.readLine())!= null)
 			{ 
 				temp = line.split(" "); //split spaces
 				for(j = 0; j<temp.length; j++)
 				{    
 					arr[i][j] =Integer.parseInt(temp[j]);
 					//    System.out.print(arr[i][j]);
 				}
 				i++;
 			}
 			buffer.close();
		} catch(Exception ex){
 			ex.printStackTrace();
 		}     
 	}
 
 	private static int indexOf(char c)
 	{
 		if(c=='@')
 			return 26;
 		else
 			return (int)(Character.toUpperCase(c))-65;
 	}
 
 	int del(char x,char y) {
 		return del[indexOf(x)][indexOf(y)];
 	}
 
 	int add(char x,char y) {
 		return add[indexOf(x)][indexOf(y)];
 	}
 
 	int sub(char x,char y) {
 		return sub[indexOf(x)][indexOf(y)];
 	}
 
 	int rev(char x,char y) {
 		return rev[indexOf(x)][indexOf(y)];
 	}
 
 
 }
