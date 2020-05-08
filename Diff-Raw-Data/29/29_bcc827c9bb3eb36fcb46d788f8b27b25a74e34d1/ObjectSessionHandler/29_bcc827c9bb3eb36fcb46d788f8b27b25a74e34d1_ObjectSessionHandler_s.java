 package org.ebag.runtime;
 
 import org.apache.mina.common.IoHandlerAdapter;
 import org.apache.mina.common.IoSession;
 import org.ebag.net.request.AnswerUpload;
 import org.ebag.net.request.ExamActivityRequet;
 import org.ebag.net.request.ExamRequet;
 import org.ebag.net.request.LoginRequest;
 import org.ebag.net.request.StartExamRequest;
 import org.ebag.net.request.UserInfoRequest;
 import org.ebag.parser.TeacherUploadExamRequest;
 import org.ebag.runtime.handler.AnswerUploadHandler;
 import org.ebag.runtime.handler.ExamActivityRequestHandler;
 import org.ebag.runtime.handler.ExamRequestHandler;
 import org.ebag.runtime.handler.LoginHandler;
 import org.ebag.runtime.handler.StartExamRequestHandler;
 import org.ebag.runtime.handler.TeacherUploadRequestHandler;
 import org.ebag.runtime.handler.UserRequestHandler;
 
 public class ObjectSessionHandler extends IoHandlerAdapter {
 
 	@Override
 	public void sessionOpened(IoSession session) {
 		System.out.println("session from :" + session.getRemoteAddress());
 	}
 
 	@Override
 	public void sessionClosed(IoSession session) {
 		System.out.println("session closed :" + session.getRemoteAddress());
 	}
 
 	@Override
 	public void messageReceived(IoSession session, Object message) {
 		System.out.println("message received :" + message.toString());
 		if(message instanceof TeacherUploadExamRequest){
 			TeacherUploadExamRequest request=(TeacherUploadExamRequest)message;
 			new TeacherUploadRequestHandler(session, request).handle();
 		}
 		else if(message instanceof LoginRequest){
 			new LoginHandler(session, message).handle();
 		}else if(message instanceof UserInfoRequest){
 			new UserRequestHandler(session, message).handle();
 		}else if(message instanceof ExamActivityRequet){
 			new ExamActivityRequestHandler(session, message).handle();
 		}else if(message instanceof ExamRequet){
 			new ExamRequestHandler(session, message).handle();
 		}else if(message instanceof StartExamRequest){
 			new StartExamRequestHandler(session, message).handle();
 		}else if(message instanceof AnswerUpload){
 			new AnswerUploadHandler(session, message).handle();
 		}
 	}
 }
