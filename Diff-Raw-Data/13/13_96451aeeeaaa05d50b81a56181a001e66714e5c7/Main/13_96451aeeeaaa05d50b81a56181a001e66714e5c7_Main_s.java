 package org.blockout;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.text.ParseException;
 
 import org.blockout.common.TileCoordinate;
 import org.blockout.network.reworked.ConnectionManager;
 import org.blockout.ui.Camera;
 import org.blockout.world.IWorld;
 import org.blockout.world.LocalGameState;
 import org.blockout.world.entity.Player;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.util.Log;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.ParameterException;
 
 import de.lessvoid.nifty.slick2d.loaders.SlickAddLoaderLocation;
 import de.lessvoid.nifty.slick2d.loaders.SlickRenderImageLoaders;
 
 /**
  * Main class for bootstrapping Spring's application context and thereby the
  * whole game.
  * 
  * @author Marc-Christian Schulze
  * 
  */
 public class Main {
 
 	private static final Logger	logger;
 	static {
 		logger = LoggerFactory.getLogger( Main.class );
 	}
 
 	public static void main( final String[] args ) throws SlickException, ParseException, UnknownHostException,
 			IOException {
 
 		Arguments arguments = parseArgumentsOrExit( args );
 
 		try {
 			if ( arguments.isHeadless() ) {
 				runInHeadlessMode( arguments );
 			} else if ( arguments.isSpectator() ) {
 				runInSpectatorMode( arguments );
 			} else {
 				runInUIMode( arguments );
 			}
 
 		} catch ( Exception e ) {
 			logger.error( "Program terminated due to exception.", e );
 		}
 	}
 
 	private static void runInSpectatorMode( final Arguments arguments ) throws SlickException {
 		logger.info( "Starting in spectator mode..." );
 		java.util.logging.Logger.getAnonymousLogger().getParent().setLevel( java.util.logging.Level.SEVERE );
 		java.util.logging.Logger.getLogger( "de.lessvoid.nifty.*" ).setLevel( java.util.logging.Level.SEVERE );
 
 		ClassPathXmlApplicationContext context;
 		context = new ClassPathXmlApplicationContext( "spectator-context.xml" );
 
 		Log.setLogSystem( new SLF4JLogSystem() );
 		AppGameContainer app = context.getBean( AppGameContainer.class );
 
 		SlickRenderImageLoaders.getInstance().loadDefaultLoaders( SlickAddLoaderLocation.last );
 
 		app.setDisplayMode( 1024, 768, false );
 		app.setAlwaysRender( true );
 		app.start();
 	}
 
 	private static void runInHeadlessMode( final Arguments arguments ) {
 		logger.info( "Starting in headless mode..." );
 		ApplicationContext context = new ClassPathXmlApplicationContext( "headless-context.xml" );
 
 		Player player = new Player( arguments.getHeadlessCmd().getPlayerName() );
 
 		LocalGameState gameState = context.getBean( LocalGameState.class );
 		IWorld world = context.getBean( IWorld.class );
 		Camera camera = context.getBean( Camera.class );
 
		InetSocketAddress address = new InetSocketAddress( arguments.getHeadlessCmd().getBootstrapAddress(), arguments
				.getHeadlessCmd().getBootstrapPort() );
		logger.info( "Bootstrapping from " + address );
		ConnectionManager connectionManager = context.getBean( ConnectionManager.class );
		connectionManager.connectTo( address );
 
 		try {
 			Thread.sleep( 6000 );
 		} catch ( InterruptedException e ) {
 			logger.warn( "Interrupted during network initialization. Network might not be initialized.", e );
 		}
 		gameState.setPlayer( player );
 		world.init( player );
 
 		try {
 			Thread.sleep( 1000 );
 		} catch ( InterruptedException e ) {
 			logger.warn( "Interrupted during world initialization. World might not be initialized.", e );
 		}
 
 		TileCoordinate playerPos = world.findTile( player );
 		camera.setViewCenter( playerPos.getX() + 0.5f, playerPos.getY() + 0.5f );
 
 		gameState.setGameInitialized( true );
 	}
 
 	private static void runInUIMode( final Arguments arguments ) throws SlickException {
 		logger.info( "Starting in UI based mode..." );
 		java.util.logging.Logger.getAnonymousLogger().getParent().setLevel( java.util.logging.Level.SEVERE );
 		java.util.logging.Logger.getLogger( "de.lessvoid.nifty.*" ).setLevel( java.util.logging.Level.SEVERE );
 
 		ClassPathXmlApplicationContext context;
 		context = new ClassPathXmlApplicationContext( "application.xml" );
 
 		Log.setLogSystem( new SLF4JLogSystem() );
 		AppGameContainer app = context.getBean( AppGameContainer.class );
 
 		SlickRenderImageLoaders.getInstance().loadDefaultLoaders( SlickAddLoaderLocation.last );
 
 		app.setDisplayMode( 1024, 768, false );
 		app.setAlwaysRender( true );
 		app.start();
 	}
 
 	private static Arguments parseArgumentsOrExit( final String[] args ) {
 		Arguments arguments = new Arguments();
 		JCommander commander = new JCommander( arguments );
 		String headlessName = "--headless";
 		commander.addCommand( headlessName, arguments.getHeadlessCmd() );
 		String spectatorName = "--spectator";
 		commander.addCommand( spectatorName, arguments.getSpectatorCmd() );
 
 		try {
 			commander.parse( args );
 			if ( arguments.isShowHelp() ) {
 				commander.usage();
 				System.exit( 0 );
 			}
 			if ( commander.getParsedCommand() != null ) {
 				arguments.setHeadless( commander.getParsedCommand().equals( headlessName ) );
 				arguments.setSpectator( commander.getParsedCommand().equals( spectatorName ) );
 			}
 
 		} catch ( ParameterException e ) {
 			System.out.println( e.getMessage() );
 			commander.usage();
 			System.exit( -1 );
 		}
 		return arguments;
 	}
 
 }
