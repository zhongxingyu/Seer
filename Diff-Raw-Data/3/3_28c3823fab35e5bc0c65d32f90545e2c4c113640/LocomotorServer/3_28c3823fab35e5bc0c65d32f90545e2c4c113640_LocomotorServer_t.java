 package org.flupes.ljf.grannyroomba.net;
 
 import org.flupes.ljf.grannyroomba.IRoombaLocomotor;
 import org.flupes.ljf.grannyroomba.messages.CommandStatusProto.CommandStatus;
 import org.flupes.ljf.grannyroomba.messages.CommandStatusProto.CommandStatus.Status;
 import org.flupes.ljf.grannyroomba.messages.DriveVelocityProto.DriveVelocityMsg;
 import org.flupes.ljf.grannyroomba.messages.LocomotionProto.LocomotionCmd;
 import org.flupes.ljf.grannyroomba.messages.RoombaStatusProto.RoombaStatus;
 import org.zeromq.ZMQException;
 
 import com.google.protobuf.InvalidProtocolBufferException;
 
 public class LocomotorServer extends ZmqServer {
 
 	protected final IRoombaLocomotor m_locomotor;
 
 	public LocomotorServer(int port, IRoombaLocomotor loco) {
 		super(port);
 		m_locomotor = loco;
 	}
 
 	@Override
 	public void loop() throws InterruptedException {
 		LocomotionCmd cmd = null;
 		try {
 			byte[] data = m_socket.recv();
 			cmd = LocomotionCmd.parseFrom(data); 
 			m_cmdid += 1;
 			switch ( cmd.getCmd() ) {
 
 			case STOP:
 				m_locomotor.stop(cmd.getStop().getMode().getNumber());
 				CommandStatus.Builder builder1 = CommandStatus.newBuilder();
 				builder1.setId(m_cmdid).setStatus(Status.BUSY);
 				m_socket.send(builder1.build().toByteArray());
 				break;
 
 			case DRIVE_VELOCITY:
 				DriveVelocityMsg msg = cmd.getDriveVelocity();
 				m_locomotor.driveVelocity(msg.getSpeed(), msg.getCurvature(), msg.getTimeout());
 				CommandStatus.Builder builder2 = CommandStatus.newBuilder();
 				builder2.setId(m_cmdid).setStatus(Status.BUSY);
 				m_socket.send(builder2.build().toByteArray());
 				break;
 
 			case STATUS_REQUEST:
 				RoombaStatus.Builder builder3 = RoombaStatus.newBuilder();
 				m_locomotor.getStatus();
 				builder3.setOimode(m_locomotor.getOiMode());
 				builder3.setBumps(m_locomotor.getBumps());
 				builder3.setVelocity(m_locomotor.getVelocity());
 				builder3.setRadius(m_locomotor.getRadius());
 				m_socket.send(builder3.build().toByteArray());
				break;

 			default:
 				s_logger.warn("LocomotionCmd " + cmd.getCmd() + " not supported!");
 			} // switch cmd.getCmd
 		}
 		catch (ZMQException e) {
 			//			if ( zmq.ZError.EAGAIN != e.getErrorCode() ) {
 			//				s_logger.info("loop received an exception different from EAGAIN -> stop now!");
 			//			}
 			if ( zmq.ZError.ETERM == e.getErrorCode() ) {
 				s_logger.debug("LocomotorServer received ETERM exception while waiting for command");
 				// Mark the service has stopped in case of the ETERM was not issued
 				// internally by cancel, but by another process
 				//				m_state = State.STOPPED;
 			}
 			else {
 				s_logger.warn("Received unexpected exception: " + e.getErrorCode());
 				s_logger.warn("  -> ignore silently for now!");
 			}
 		}
 		catch (InvalidProtocolBufferException e) {
 			s_logger.warn("Could not decode LocomotionCmd message properly!");
 			s_logger.warn("  -> ignore silently for now!");
 		}
 		if ( cmd == null ) {
 			s_logger.debug("locomotor server was interrupted before receiving a command");
 		}
 	}
 
 	@Override
 	public int fini() {
 		int ret = super.fini();
 		s_logger.info("LocomotorServer canceled");
 		return ret;
 	}
 }
