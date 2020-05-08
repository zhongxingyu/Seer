 /**
  * 
  */
 package org.promasi.client.playmode.multiplayer.client.clientstate;
 
 import java.beans.XMLDecoder;
 import java.io.ByteArrayInputStream;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Shell;
 import org.joda.time.DateTime;
 import org.promasi.client.gui.IWaitingGameDialogListener;
 import org.promasi.client.gui.WaitingGameDialog;
 import org.promasi.client.playmode.multiplayer.AbstractClientState;
 import org.promasi.client.playmode.multiplayer.MultiPlayerGame;
 import org.promasi.client.playmode.multiplayer.ProMaSiClient;
 import org.promasi.protocol.messages.GameCanceledRequest;
 import org.promasi.protocol.messages.GameCanceledResponse;
 import org.promasi.protocol.messages.GameStartedRequest;
 import org.promasi.protocol.messages.GameStartedResponse;
 import org.promasi.protocol.messages.LeaveGameRequest;
 import org.promasi.protocol.messages.LeaveGameResponse;
 import org.promasi.protocol.messages.MessageRequest;
 import org.promasi.protocol.messages.UpdateGameListRequest;
 import org.promasi.protocol.messages.UpdateGamePlayersListRequest;
 import org.promasi.protocol.messages.WrongProtocolResponse;
 import org.promasi.utilities.exceptions.NullArgumentException;
 
 /**
  * @author m1cRo
  *
  */
 public class WaitingGameClientState extends AbstractClientState implements IWaitingGameDialogListener
 {
 	/**
 	 * 
 	 */
 	private WaitingGameDialog _waitingGameDialog;
 	
 	/**
 	 * 
 	 */
 	private ProMaSiClient _client;
 	
 	/**
 	 * 
 	 */
 	private Shell _shell;
 	
 	/**
 	 * 
 	 */
 	private String _gameName;
 	
 	/**
 	 * 
 	 */
 	private String _gameDescription;
 	
 	/**
 	 * 
 	 */
 	private List<String> _players;
 	
 	/**
 	 * 
 	 * @param client
 	 * @throws NullArgumentException
 	 */
 	public WaitingGameClientState(Shell shell,ProMaSiClient client, String gameName, String gameDescription,List<String> players)throws NullArgumentException{
 		if(client==null){
 			throw new NullArgumentException("Wrong argument client==null");
 		}
 		
 		if(players==null){
 			throw new NullArgumentException("Wrong argument players==null");
 		}
 		
 		if(gameName==null){
 			throw new NullArgumentException("Wrong argument gameName==null");
 		}
 		
 		if(gameDescription==null){
 			throw new NullArgumentException("Wrong argument gameDescription==null");
 		}
 		
 		_client=client;
 		_shell=shell;
 		_gameName=gameName;
 		_gameDescription=gameDescription;
 		_players=players;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.promasi.playmode.multiplayer.IClientState#onReceive(org.promasi.playmode.multiplayer.ProMaSiClient, java.lang.String)
 	 */
 	@Override
 	public void onReceive(ProMaSiClient client, String recData) {
 		try{
 			Object object=new XMLDecoder(new ByteArrayInputStream(recData.getBytes())).readObject();
 			if(object instanceof GameStartedRequest){
 				GameStartedRequest request=(GameStartedRequest)object;
 				if(request.getGameModel()==null || request.getDateTime()==null){
 					client.sendMessage(new WrongProtocolResponse().serialize());
 					client.disconnect();
 					return;
 				}
 				
 				PlayingGameClientState state=new PlayingGameClientState(_shell, new MultiPlayerGame(client),client, request.getGameModel(),new DateTime(request.getDateTime()));
 				changeClientState(client, state);
 				client.sendMessage(new GameStartedResponse().serialize());
 				_waitingGameDialog.close();
 			}else if(object instanceof UpdateGamePlayersListRequest){
 				UpdateGamePlayersListRequest request=(UpdateGamePlayersListRequest)object;
 				_waitingGameDialog.updatePlayersList(request.getPlayers());
 			}else if(object instanceof MessageRequest){
 				MessageRequest request=(MessageRequest)object;
 				if(request.getClientId()==null || request.getMessage()==null){
 					client.sendMessage(new WrongProtocolResponse().serialize());
 					client.disconnect();
 				}else{
 					_waitingGameDialog.messageReceived(request.getClientId(), request.getMessage());
 				}
 			}else if(object instanceof MessageRequest){
 				MessageRequest request=(MessageRequest)object;
 				if(request.getClientId()==null || request.getMessage()==null){
 					client.sendMessage(new WrongProtocolResponse().serialize());
 					client.disconnect();
 				}else{
 					_waitingGameDialog.messageReceived(request.getClientId(), request.getMessage());
 				}
 			}else if(object instanceof GameCanceledRequest){
 				ChooseGameClientState clientState=new ChooseGameClientState(client, _shell, new HashMap<String,String>());
 				client.sendMessage(new GameCanceledResponse().serialize());
 				changeClientState(client, clientState);
 			}else if(object instanceof UpdateGameListRequest){
 			}else if(object instanceof LeaveGameResponse){
 				changeClientState(client, new ChooseGameClientState(client, _shell, new HashMap<String,String>()));
 			}else{
 				_client.sendMessage(new WrongProtocolResponse().serialize());
 				_waitingGameDialog.close();
 			}
 		}catch(NullArgumentException e){
 			client.disconnect();
 		}catch(IllegalArgumentException e){
 			client.disconnect();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.promasi.playmode.multiplayer.IClientState#onSetState(org.promasi.playmode.multiplayer.ProMaSiClient)
 	 */
 	@Override
 	public void onSetState(ProMaSiClient client) {
 		if(!_shell.isDisposed() && !_shell.getDisplay().isDisposed()){
 			_shell.getDisplay().asyncExec(new Runnable() {
 				
 				@Override
 				public void run() {
 					try {
 						_waitingGameDialog=new WaitingGameDialog(_shell, SWT.DIALOG_TRIM, _gameName, _gameDescription, WaitingGameClientState.this,_players);
 						_waitingGameDialog.open();
 					} catch (NullArgumentException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			});
 		}
 	}
 
 	@Override
 	public void sendButtonPressed(String messageText) {
 		_client.sendMessage(new MessageRequest(null, messageText).serialize());
 	}
 
 	@Override
 	public void onDisconnect(ProMaSiClient client) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onConnect(ProMaSiClient client) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onConnectionError(ProMaSiClient client) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void dialogClosed(WaitingGameDialog dialog) {
 		_client.sendMessage(new LeaveGameRequest().serialize());
 	}
 }
