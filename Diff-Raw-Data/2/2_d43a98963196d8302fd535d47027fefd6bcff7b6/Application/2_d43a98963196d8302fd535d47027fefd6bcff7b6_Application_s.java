 package app;
 
 import vam.PortAudioPlayer;
 import vam.VorbisFileReader;
 
 public class Application
 {
 	public static final int RESULT_SUCCESS				= 0;
 	public static final int RESULT_NO_INPUT_FILE		= 1;
 	public static final int RESULT_VORBIS_ERROR			= 2;
 
 	private static String twoDig(int value)
 	{
 		if (value >= 10) 
 			return String.valueOf(value);
 		else 
 			return "0" + String.valueOf(value);
 	}
 	
 	public static void main(String[] args)
 	{
 		try
 		{
 			if (args.length == 0)
 			{
 				System.err.println("No input file");
 				System.exit(RESULT_NO_INPUT_FILE);
 				return;
 			}
 			
 			System.out.println("Input file: " + args[0]);
 
 			VorbisFileReader vfr = new VorbisFileReader(args[0], 64);
 
 			System.out.println("Encoded by " + vfr.getVendor());
 			System.out.println("\nBitstream has " + vfr.getChannels() + " channels, " + vfr.getRate() + "Hz, quality is " + (int)(Math.round((float)vfr.getBitsPerSecond() / 1000)) + "Kbps (on average)");
 
 			String[] comments = vfr.getComments(); 
 			if (comments.length > 0) System.out.println("\nComments:");
 			for (int i = 0; i < comments.length; i++)
 			{
 				System.out.println("  " + comments[i]);
 			}
 			
			PortAudioPlayer pap = new PortAudioPlayer(vfr.getChannels(), vfr.getRate(), 64);
 
 			pap.setSoundSource(vfr);
 			pap.play();
 			
 			System.out.println();
 			while (vfr.getState() != VorbisFileReader.State.sEndOfData)
 			{
 				try
 				{
 					Thread.sleep(10);
 					
 					int min = (int)(vfr.getPlayhead()) / 60;
 					int sec = (int)(vfr.getPlayhead()) % 60;
 					int sp10 = (int)(vfr.getPlayhead() * 100) % 100;
 					
 					int lmin = (int)(vfr.getLength()) / 60;
 					int lsec = (int)(vfr.getLength()) % 60;
 					int lsp10 = (int)(vfr.getLength() * 100) % 100;
 					
 					System.out.print("Playing the file... [ " + twoDig(min) + ":" + twoDig(sec) + "." + twoDig(sp10) + " / " + twoDig(lmin) + ":" + twoDig(lsec) + "." + twoDig(lsp10) + " ]\r");
 					
 				} 
 				catch (InterruptedException e)
 				{
 					e.printStackTrace();
 				}
 			}
 			
 			pap.close();
 			vfr.close();
 			
 			System.out.println("\nBye.");
 			System.exit(RESULT_SUCCESS);
 		} 
 		catch (VorbisFileReader.Error e)
 		{
 			e.printStackTrace();
 			System.exit(RESULT_VORBIS_ERROR);
 		}
 	}
 }
