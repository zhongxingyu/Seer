 package controllers.server;
 
 import channels.SocketChannel;
 import channels.server.SocketServer;
 import controllers.ConnectionFactory;
 import controllers.WorkFlow;
 import org.junit.Before;
 import org.junit.Test;
 import views.server.ServerView;
 
 import static org.mockito.Mockito.*;
 
 public class ServerControllerTest
 {
   ServerController controller;
   WorkFlow workFlow;
   ConnectionFactory factory;
   ServerView view;
   SocketChannel channel;
   SocketServer socketServer;
 
   @Before
   public void setup()
   {
     workFlow = mock(WorkFlow.class);
     factory = mock(ConnectionFactory.class);
     controller = ServerController.createController(workFlow, factory);
     view = mock(ServerView.class);
     controller.bind(view);
     channel = mock(SocketChannel.class);
     socketServer = mock(SocketServer.class);
     stub(factory.createServer(1234, controller)).toReturn(socketServer);
   }
 
   @Test
   public void run_will_start_the_server()
   {
     controller.start();
     verify(view).show();
     verify(socketServer).start();
   }
 
   @Test
   public void stopServer_will_close_channels_and_server_and_move_to_home_screen()
   {
     controller.bind(view);
     controller.start();
     controller.onConnectionEstablished(channel);
     controller.stopServer();
     verify(channel).stop();
     verify(socketServer).stop();
     verify(workFlow).goHome();
   }
 
//  @Test
//  public void startGameWindow_will_stop_the_serverSocket_and_calls_start_game()
//  {
//    controller.bind(view);
//    controller.start();
//    controller.startGame();
//
//    verify(socketServer).stop();
//    verify(workFlow).startGame(new ArrayList<Player>());
//  }
 
   @Test
   public void setListData_will_set_the_list_of_data_in_view()
   {
     controller.bind(view);
     controller.start();
     controller.setListData();
 
     verify(view).setListData(new String[]{});
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void cannot_create_serverController_of_null_workflow()
   {
     controller = ServerController.createController(null,factory);
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void cannot_create_serverController_of_null_factory()
   {
     controller = ServerController.createController(workFlow,null);
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void cannot_bind_null_to_the_controller()
   {
     controller.bind(null);
   }
 }
 
 
