import java.nio.charset.Charset;

 
 public class LZ77 {
 	
 	public static class Vocabulary {
 		public Vocabulary( int maxLength ) {
 			maxSize = maxLength;
 			str = new String();
 		}
 		
 		public void input(char c) {
 			if ( str.length() == maxSize ) {
 				str = str.substring(1) + c;
 			}else {
 				str += c;
 			}
 		}
 		
 		public int findWord( String s ) {
 			return str.indexOf(s);
 		}
 		
 		public void setVocabulary( String s ) {
 			if ( s.length() <= maxSize ) {
 				str = s;
 			}else {
 				str = s.substring(s.length() - maxSize);
 			}
 		}
 		
 		public int getSize() {
 			return maxSize;
 		}
 		
 		private int maxSize;
 		private String str;
 	}
 	
 	static class MyOutput {
 		public String line, writeBlock, blockPos, blockSize, binOut;
 		public void out() {
 			System.out.printf("%3s\t%10s\t%3s\t%3s\t%s\n", line, writeBlock, blockPos, blockSize, binOut );
 		}
 	}
 	
 	public static String charToBinary(char c) {
 		StringBuilder binary = new StringBuilder();
		//if ()
		if ( c != ' ' && c != '.'  ) {
			c <<= 8;
			for (int i = 0; i < 8; i++) {
				binary.append((c & 32768) == 0 ? 0 : 1);
				c <<= 1;
			}
		}else {
			for (int i = 0; i < 8; i++) {
				binary.append((c & 32768) == 0 ? 0 : 1);
				c <<= 1;
			}
 		}
 		binary.append(' ');
 		return binary.toString();
 	}
 	
 	public static String lengthOutput(int len, int vSize) {
 		if (len == 1) {
 			return "1";
 		}
 		if (len == 2) {
 			return "01";
 		}
 		if (len == 3) {
 			return "001";
 		}
 		return "000"+distanceOutput(len, vSize);
 	}
 	
 	public static String distanceOutput( int dist, int vSize ) {
 		int k = (int)(0.9999999999999+Math.log(vSize)/Math.log(2));
 		String bin = Integer.toBinaryString(dist);
 		if ( bin.length() < k ) {
 			String add = "";
 			for (int i=0; i < k - bin.length(); ++i) {
 				add += "0";
 			}
 			add += bin;
 			return add;
 		}else {
 			return bin;
 		}
 	}
 	
 	public static int tryBlock( String msg, int i, Vocabulary voc, MyOutput output, StringBuilder binaryOutput ) {
 		int len = 1;
 		int oldPos = -1;
 		int pos = voc.findWord(msg.substring(i, i+len));
 		while ( pos >= 0 ) {
 			++len;
 			oldPos = pos;
 			pos = voc.findWord(msg.substring(i, i+len));
 		}
 		--len;
 		if (oldPos == -1) {
 			output.writeBlock = "+" + msg.charAt(i);
 			output.blockPos = "";
 			output.blockSize = "0";
 			output.binOut = "0";
 			String tmpStr = charToBinary(msg.charAt(i));//.substring(8);
 			output.binOut += "+" + tmpStr;
 			binaryOutput.append("0"+charToBinary(msg.charAt(i)));
 		}else {
 			output.writeBlock = msg.substring(i, i+len);
 			output.blockPos = Integer.toString(oldPos);
 			output.blockSize = Integer.toString(len);
 			output.binOut = "1+"+distanceOutput( oldPos, voc.getSize())+"+"+lengthOutput(len, voc.getSize());
 			binaryOutput.append("1"+distanceOutput( oldPos, voc.getSize()) + lengthOutput(len, voc.getSize()));
 			//output.binOut = "1+"+distanceOutput( oldPos, voc.getSize())+"+len";
 		}
 		
 		return len + 1;
 	}
 	
 	public static void main( String args[] ) {
		
		//Charset utf8charset = Charset.forName("UTF-8"); 
		//Charset iso88591charset = Charset.forName("ISO-8859-1"); 
		//ByteBuffer inputBuffer = ByteBuffer.wrap(new Byte[]{(byte)0xC3, (byte)0xA2}); 
		// decode UTF-8 CharBuffer data = utf8charset.decode(inputBuffer); // encode ISO-8559-1 ByteBuffer outputBuffer = iso88591charset.encode(data); byte[] outputData = outputBuffer.array();
		
 		Vocabulary vocabulary = new Vocabulary(Integer.parseInt(args[0]));
 		String msg = args[1];
 		int i = 2;
 		while ( i < args.length )
 		{
 			msg += " " + args[i];
 			++i;
 		}
 		
 		StringBuilder binaryOutput = new StringBuilder();
 		i = 0;
 		MyOutput outputTable = new MyOutput();
 		while ( i < msg.length() ) {
 			i += tryBlock( msg, i, vocabulary, outputTable, binaryOutput);
 			vocabulary.setVocabulary(msg.substring(0, i));
 			outputTable.line = Integer.toString(i);
 			outputTable.out();
 		}
		System.out.println("Source size = " + msg.length() + " bytes");
 		int arcSize = (binaryOutput.length() + 7)/8;
 		System.out.println("Archived size = " + arcSize + " bytes");
 	}
 
 }
