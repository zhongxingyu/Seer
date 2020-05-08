 package org.ebag.runtime.handler;
 
 import java.util.LinkedHashSet;
 import java.util.List;
 
 import org.apache.mina.common.IoSession;
 import org.ebag.net.obj.I;
 import org.ebag.net.request.LoginRequest;
 import org.ebag.net.response.LoginResponse;
 
 import ebag.pojo.Eclass;
 import ebag.pojo.Euser;
 import ebag.pojo.EuserDAO;
 
 public class LoginHandler extends BasicHandler<LoginRequest> {

 	public LoginHandler(IoSession session, Object message) {
 		super(session, message);
 	}
 
 	@Override
 	public void handle() {
 		LoginResponse response = new LoginResponse();
 		response.setResult(I.signal.login_false);
 		try {
 			String name = message.getUname();
 			int uid = message.getUid();
 			String pwd = message.getUpwd();

 			EuserDAO udao = new EuserDAO();
 			if (name != null && !name.equals("")) {
 				List<Euser> ulist = udao.findByName(name);
 				if(ulist!=null&&ulist.size()!=0){
 					if(ulist.get(0).getPwd().equals(pwd)){
 						response.setUser(ulist.get(0));
 						response.setResult(I.signal.login_true);
 						LinkedHashSet<Eclass> set=new LinkedHashSet<Eclass>();
 						set.addAll(ulist.get(0).getEclasses());
 						response.setClassSet(set);
 					}
 				}
 			}else {
 				Euser u=udao.findById(uid);
 				if(u.getPwd().equals(pwd)){
 					response.setUser(u);
 					response.setResult(I.signal.login_true);
 					LinkedHashSet<Eclass> set=new LinkedHashSet<Eclass>();
 					set.addAll(u.getEclasses());
 					response.setClassSet(set);
 				}
 			}
 		} catch (Exception e) {
 			session.write(response);
 			return;
 		}
 
 		session.write(response);
 	}
 
 }
