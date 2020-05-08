 import java.util.ArrayList;
 import java.io.*;
 
 /**
  * 
  * @author Coleman
  * This class provides a wrapper for the Graph object and parses all input files.
  */
 public class GraphDriver	{
 	
 	// only need one graph
 	private static Graph mygraph;
 	
 	/**
 	 * Prints a path of actors from an ArrayList.
 	 * @param path An ArrayList returned from shortestPath or the like.
 	 */
 	private static void printPath ( ArrayList path )	{
 		for ( int x = 0; x < path.size() - 1; x++ )	{
 			Comparable one = ((GraphNode)path.get ( x )).getKey();
 			Comparable two = ((GraphNode)path.get ( x + 1 )).getKey();
 			String movie = mygraph.getMovie ( one, two );
			if ( !movie.equals ( "(0)" ) )
				System.out.println ( "'" + one.toString() + "' starred with '" + two.toString() + "' in the movie '" + movie + "'" );
 		}
 	}
 	
 	/**
 	 * Parses and inserts all data from an input file.
 	 * @param filename Name of the file to be read.
 	 * @param filenum Number of the file in the sequence, can be -1 if not known.
 	 * @param len Total number of input files.
 	 */
 	private static void readFile ( String filename, int filenum, int len )	{
 		BufferedReader read = null;
 		ArrayList actors = new ArrayList();
 		try	{
 			String t = "";
 			if ( filenum != -1 )	{
 				t = " (" + (filenum + 1) + "/" + len + ")";
 			}
 			System.out.println ( "***Reading from file: " + filename + t );
 			read = new BufferedReader ( new FileReader ( filename ) );
 			//this big loop reads in the movie name and all the actors
 			while ( read.ready() )	{
 				String line = read.readLine();
 				if ( line.equals ( "" ) )	continue;
 				//split the date off the movie name, makes for easier tie breaking.
 				String data[] = line.split ( "\\(\\d{4}\\)\\s*$" );
 				String date =  line.substring ( data[0].length() + 1, data[0].length() + 5 );
 				data[0].trim();
 				
 				//get all the actors in a usable form
 				line = read.readLine();
 				while ( !line.equals ( "" ) )	{
 					actors.add ( line );
 					line = read.readLine();
 				}
 				String actor[] = new String[actors.size()];
 				actor = (String [])actors.toArray ( actor );
 				//now we must add all the actors into the graph
 				for ( int x = 0; x < actor.length; x++ )	{
 					try	{
 						mygraph.addVertex ( new GraphNode ( actor[x] ) );
 					}
 					catch ( GraphException exception )	{}
 				}
 				//and add an edge between all the actors for this movie
 				for ( int x = 0; x < actor.length - 1; x++ )	{
 					for ( int y = x + 1; y < actor.length; y++ )	{
 						try	{
 							mygraph.addEdge ( actor[x], actor[y], data[0], date );
 						}
 						catch ( GraphException exception )	{}
 					}
 				}
 				//start all over again
 				actors.clear();
 			}// end while
 		}
 		catch ( IOException exception )	{
 			System.out.println ( "Error reading the file: " + filename );
 			exception.printStackTrace();
 		}
 	}
 	
 	public static void main ( String[] args )	{  
 		if ( args.length < 1 )	{
 			System.out.println ( "Usage: java GraphDriver [input files]" );
 			System.exit ( 1 );
 		}
 		
 		mygraph = new Graph();
 		
 		for ( int filenum = 0; filenum < args.length; filenum++ )	{
 			readFile ( args[filenum], filenum, args.length );
 		}
 		
 		/* this block is the user interface. it is simple, but yet still allows for all
 		 * the neccessary functionality of the project.
 		 */
 		String command = "";
 		while ( !command.equals ( "quit" ) )	{
 			System.out.println ( "*Commands*\t*Description*" );
 			System.out.println ( "path\t\tDjikstra's Shortest Path" );
 			System.out.println ( "bfs\t\tBreadth-First Search" );
 			System.out.println ( "add\t\tUpdate Graph From File" );
 			System.out.println ( "dia\t\tCompute Diameter Of Graph" );
 			System.out.println ( "quit\t\tQuit" );
 			System.out.print ( "> " );
 			BufferedReader in = new BufferedReader ( new InputStreamReader ( System.in ) );
 			try	{
 				command = in.readLine().trim();
 			}
 			catch ( IOException exception )	{
 				System.out.println ( "bailing from:" );
 				exception.printStackTrace();
 				break;
 			}
 			if ( command.equals( "path" ) )	{
 				System.out.print ( "actors (one,two)> " );
 				String actors[] = null;
 				try	{
 					actors = (String [])in.readLine().trim().split ( "," );
 				}
 				catch ( IOException exception )	{
 					System.out.println ( "bailing from:" );
 					exception.printStackTrace();
 					break;
 				}
 				if ( actors.length != 2 )	{
 					System.out.println ( "Enter two actors only!" );
 				}
 				ArrayList path = mygraph.shortestPath ( actors[0].trim(), actors[1].trim() );
 				printPath ( path );
 			}
 			else if ( command.equals( "bfs" ) )	{
 				System.out.print ( "actors (one,two)> " );
 				String actors[] = null;
 				try	{
 					actors = (String [])in.readLine().trim().split ( "," );
 				}
 				catch ( IOException exception )	{
 					System.out.println ( "bailing from:" );
 					exception.printStackTrace();
 					break;
 				}
 				if ( actors.length != 2 )	{
 					System.out.println ( "Enter two actors only!" );
 				}
 				//put in real bfs here for two states
 				ArrayList path = mygraph.bfs ( actors[0].trim(), actors[1].trim() );
				printPath ( path );
 			}
 			else if ( command.equals ( "add" ) )	{
 				System.out.print ( "read from file> " );
 				String filename[] = null;
 				try	{
 					filename = (String [])in.readLine().trim().split ( " " );
 				}
 				catch ( IOException exception )	{
 					System.out.println ( "bailing from:" );
 					exception.printStackTrace();
 					break;
 				}
 				for ( int q = 0; q < filename.length; q++ )	{
 					readFile ( filename[q], q, filename.length );
 				}
 			}
 			else if ( command.equals ( "dia" ) )	{
 				System.out.println ( "Please wait while searching through " + mygraph.numVertices() + " vertecies..." );
 				System.out.println ( "The diameter of the graph is " + mygraph.diameter() );
 			}
 			else if ( command.equals ( "quit" ) )	{
 				break;
 			}
 			System.out.println();
 		}
 	}
 }
