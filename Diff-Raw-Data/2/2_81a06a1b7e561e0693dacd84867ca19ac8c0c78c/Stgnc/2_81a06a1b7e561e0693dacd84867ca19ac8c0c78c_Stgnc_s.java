 package lud.stgn;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.ParseException;
 import java.util.Arrays;
 import javax.imageio.ImageIO;
 
 public class Stgnc {
 
 	/*
 	 * provides console interface for Stgn
 	 */
 
 	/*
 	 * conceal secret file into container image file, save result as
 	 * ihaveasecret file
 	 * 
 	 * assuming args[] are prepared by parseOpts:
 	 * 
 	 * --conceal
 	 * secret.info
 	 * --into
 	 * container.png
 	 * --bitmask
 	 * 0x03030303
 	 * --saveas
 	 * ihaveasecret.png
 	 */
 	public static void conceal( String[] args )
 												throws IOException {
 
 		File container = new File( args[ 3 ] );
 		int mask = Integer.parseInt( args[ 5 ].substring( 2, 10 ), 16 );
 
 		Stgn stgn1 = new Stgn( container, mask );
 		File secretFile = new File( args[ 1 ] );
 
 		BufferedImage resultImg = stgn1.conceal( secretFile );
 		File saveAsFile = new File( args[ 7 ] );
 		ImageIO.write( resultImg, "png", saveAsFile );
 
 		System.out.println( "("
 			+ ( new File( args[ 1 ] ).length() )
 			+ ") bytes concealed into '"
 			+ args[ 3 ]
 			+ "' with mask ("
 			+ args[ 5 ]
 			+ ") and saved as '"
 			+ args[ 7 ]
 			+ "'" );
 	}
 
 	/*
 	 * unveils size bytes from ihaveasecret file, save extracted as hidden.info
 	 * file
 	 * 
 	 * assuming args[] are prepared by parseOpts:
 	 * --unveil
 	 * hidden.info
 	 * --from
 	 * ihaveasecret.png
 	 * --size
 	 * 100000
 	 * --bitmask
 	 * 0x03030303
 	 */
 	public static void unveil( String[] args )
 												throws IOException {
 
 		int mask = Util.validMask( args[ 7 ] );
 		int secretSize = Integer.parseInt( args[ 5 ] );
 		File iHaveASecret = new File( args[ 3 ] );
 		Stgn stgn2 = new Stgn( iHaveASecret, mask );
 
 		ByteArrayOutputStream bOutS =
 			( ByteArrayOutputStream ) stgn2.unveil( secretSize );
 
 		OutputStream revealed = new FileOutputStream( args[ 1 ] );
 		revealed.write( bOutS.toByteArray(), 0, secretSize );
 		System.out.println( "("
 			+ ( secretSize )
 			+ ") bytes unveiled from '"
 			+ args[ 3 ]
 			+ "' with mask ("
 			+ args[ 7 ]
 			+ ") and saved as '"
 			+ args[ 1 ]
 			+ "'" );
 		revealed.close();
 	}
 
 	static String[] parseOpts( String[] args )
 												throws ParseException {
 
 		String[] opts = new String[ 8 ];
 		try {
 
 			if ( args[ 0 ].equals( "--conceal" ) && args[ 2 ].equals( "--into" ) ) {
 
 				opts[ 0 ] = args[ 0 ];
 				opts[ 1 ] = args[ 1 ];
 				opts[ 2 ] = args[ 2 ];
 				opts[ 3 ] = args[ 3 ];
 				if ( !( args.length > 4 && args[ 4 ].equals( "--bitmask" ) ) ) {
 					opts[ 4 ] = "--bitmask";
 					opts[ 5 ] =
 						"0x" + String.format( "%08X", Stgn.DEFAULT_MASK );
 				} else {
 					opts[ 4 ] = args[ 4 ];
 					opts[ 5 ] = args[ 5 ];
 				}
 				if ( !( args.length > 6 && args[ 6 ].equals( "--saveas" ) ) ) {
 					opts[ 6 ] = "--saveas";
 					opts[ 7 ] =
 						"ihaveasecret_mask("
 							+ opts[ 5 ]
 							+ ")_size("
 							+ ( new File( opts[ 1 ] ).length() )
 							+ ").png";
 				} else {
 					opts[ 6 ] = args[ 6 ];
 					opts[ 7 ] = args[ 7 ];
 				}
 
 			} else if ( args[ 0 ].equals( "--unveil" )
 				&& args[ 2 ].equals( "--from" ) ) {
 
 				opts[ 0 ] = args[ 0 ];
 				opts[ 1 ] = args[ 1 ];
 				opts[ 2 ] = args[ 2 ];
 				opts[ 3 ] = args[ 3 ];
 				// size
 				if ( !( args.length > 4 && args[ 4 ].equals( "--size" ) ) ) {
 					throw new ParseException(
 						"--size must be specified for unveil mode",
 						0 );
 				} else {
 					opts[ 4 ] = args[ 4 ];
 					opts[ 5 ] = args[ 5 ];
 				}
 				if ( !( args.length > 6 && args[ 6 ].equals( "--bitmask" ) ) ) {
 					opts[ 6 ] = "--bitmask";
 					opts[ 7 ] =
 						"0x" + String.format( "%08X", Stgn.DEFAULT_MASK );
 				} else {
 					opts[ 6 ] = args[ 6 ];
 					opts[ 7 ] = args[ 7 ];
 				}
 			} else {
 				throw new ParseException( "Failed to parse args:"
 					+ Arrays.toString( args ), 0 );
 			}
 		} catch ( Exception e ) {
 			throw new ParseException( "Failed to parse args:"
 				+ Arrays.toString( args ), 0 );
 		}
 		return opts;
 	}
 
 	public static void main( String[] args ) {
 
 		String usage =
 			"\nStgn - hides specified file into less significant bits of container resulting 8bit ARGB png image"
 				+ "\n"
 				+ "\nUsage:"
 				+ "\n"
 				+ "\nStgn --conceal secret.info --into container.png [ --bitmask 0x03030303 ] [ --saveas ihaveasecret.png ]"
 				+ "\n"
 				+ "\n    secret.info   - an existing file, we want to hide into container"
 				+ "\n    container.png - javax.imageio readable image file (jpg/png/gif/bmp)"
				+ "\n    bitmask       - hex integer, bits for each ARGB pixel used to hide secret info. deafult: 0x03030303"
 				+ "\n    saveas        - file to be created. foramt is always 8bit ARGB png. default: ihaveasecret.png"
 				+ "\n"
 				+ "\nStgn --unveil  hidden.info --from ihaveasecret.png --size 100000 [ --bitmask 0x03030303 ]"
 				+ "\n"
 				+ "\n    hidden.info      - resulting file, contains extracted info from ihaveasecret.png"
 				+ "\n    ihaveasecret.png - existing file with hidden info inside"
 				+ "\n    size             - number of bytes to extract from ihaveasecret"
 				+ "\n    bitmask          - bits for each ARGB pixel used to hide secret info. deafult: 0x03030303"
 				+ "\n";
 		try {
 			args = parseOpts( args );
 			if ( args[ 0 ].equals( "--conceal" ) ) {
 				conceal( args );
 			} else if ( args[ 0 ].equals( "--unveil" ) ) {
 				unveil( args );
 			}
 		} catch ( ParseException pe ) {
 			System.err.println( pe + usage );
 		} catch ( Exception e ) {
 			e.printStackTrace( System.err );
 		}
 
 		// System.out.println( Arrays.toString( args ) );
 
 	}
 
 }
