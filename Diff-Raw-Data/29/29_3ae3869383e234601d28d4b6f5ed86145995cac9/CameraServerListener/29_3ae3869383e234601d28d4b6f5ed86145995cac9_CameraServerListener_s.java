 package at.fhv.audioracer.server;
 
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.imageio.ImageIO;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import at.fhv.audioracer.communication.world.message.CameraMessage;
 import at.fhv.audioracer.communication.world.message.CarDetectedMessage;
 import at.fhv.audioracer.communication.world.message.ConfigureMapMessage;
 import at.fhv.audioracer.communication.world.message.UpdateCarMessage;
 import at.fhv.audioracer.core.model.Car;
 import at.fhv.audioracer.server.game.GameModerator;
 
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.Listener;
 import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
 
 public class CameraServerListener extends Listener {
 	
 	private static Logger _logger = LoggerFactory.getLogger(CameraServerListener.class);
 	private GameModerator _moderator;
 	
 	public CameraServerListener(GameModerator moderator) {
 		_moderator = moderator;
 	}
 	
 	public void received(Connection connection, Object object) {
 		if (object instanceof CameraMessage) {
 			CameraMessage message = (CameraMessage) object;
 			
 			switch (message.messageId) {
 				case UPDATE_CAR:
 					UpdateCarMessage updateCarMsg = (UpdateCarMessage) message;
					_moderator.updateCar(updateCarMsg.carId, updateCarMsg.posX, updateCarMsg.poxY, updateCarMsg.direction);
 					break;
 				case CAR_DETECTED:
 					CarDetectedMessage carDetectedMsg = (CarDetectedMessage) message;
 					
 					// get the car image
 					BufferedImage carImage = null;
 					if (carDetectedMsg.image != null && carDetectedMsg.image.length > 0) {
 						try {
 							InputStream in = new ByteInputStream(carDetectedMsg.image, carDetectedMsg.image.length);
 							carImage = ImageIO.read(in);
 							in.close();
 						} catch (IOException e) {
 							_logger.error("Exception caught while reading car image!", e);
 						}
 						
 					} else {
 						_logger.error("No image received for car with id: {}", carDetectedMsg.carId);
 					}
 					
 					if (carImage != null) {
 						
 						// TODO: We should also send current Position and Direction on new car detected
 						
 						Car car = new Car(carDetectedMsg.carId, null, null, carImage);
 						_moderator.carDetected(car);
 					}
 					
 					break;
 				case CONFIGURE_MAP:
 					ConfigureMapMessage configureMapMsg = (ConfigureMapMessage) message;
 					_moderator.configureMap(configureMapMsg.sizeX, configureMapMsg.sizeY);
 					break;
 				case DETECTION_FINISHED:
 					_moderator.detectionFinished();
 					break;
 				default:
 					_logger.warn("Camera message with id: {} not known!", message.messageId);
 					break;
 			}
 		}
 	}
 	
 	public void disconnected(Connection connection) {
 		_logger.info("Camera connection gone.");
 	}
 }
