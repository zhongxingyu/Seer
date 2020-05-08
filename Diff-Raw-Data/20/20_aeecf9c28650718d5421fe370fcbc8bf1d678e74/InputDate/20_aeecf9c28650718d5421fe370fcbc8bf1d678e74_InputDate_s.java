 import java.io.*;
 class InputDate{
 	private BufferedReader buf = null;
 	public InputDate(){
 		this.buf = new BufferedReader(new InputStreamReader(System.in)); 
 	}
 	public String getString(String info){
 		String temp = null;
		System.out.println(info);   //打印提示信息
 		try{
 			temp = buf.readLine();
 		}catch(IOException e ){
 			e.printStackTrace();
 		}
 		return temp;
 	}
 	public int getInt(String info,String err){
 		int temp =0;
 		String str = null;
 		boolean flag = true;
 		while(flag){
 			str = this.getString(info);
 			if(str.matches("^\\d+$")){
 				temp = Integer.parseInt(str);
 				flag = false;
 			}else{
 				System.out.println(err);
 			}
 		}
 		return temp;
 	}
 }
