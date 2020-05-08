 package org.ebag.runtime.handler;
 
import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
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
 			log.info(name+","+uid+","+pwd);
 			EuserDAO udao = new EuserDAO();
 			if (name != null && !name.equals("")) {
 				List<Euser> ulist = udao.findByName(name);
 				if(ulist!=null&&ulist.size()!=0){
 					if(ulist.get(0).getPwd().equals(pwd)){
 						
 						Euser u=new Euser();
 						u.setId(ulist.get(0).getId());
 						u.setName(ulist.get(0).getName());
 						u.setPwd(ulist.get(0).getPwd());
 						u.setUserType(ulist.get(0).getUserType());
 						response.setUser(u);
 						response.setResult(I.signal.login_true);
 						LinkedHashSet<Eclass> set=changeToHashSet(ulist.get(0).getEclasses());
 						response.setClassSet(set);
 					}
 				}
 			}else {
 				Euser u=udao.findById(uid);
 				Euser u_res=new Euser();
 				if(u.getPwd().equals(pwd)){
 					u_res.setId(u.getId());
 					u_res.setName(u.getName());
 					u_res.setPwd(u.getPwd());
 					u_res.setUserType(u.getUserType());
 					response.setUser(u_res);
 					response.setResult(I.signal.login_true);
 					LinkedHashSet<Eclass> set=changeToHashSet(u.getEclasses());
 					response.setClassSet(set);
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			session.write(response);
 			return;
 		}
 
 		session.write(response);
 	}
 	
 	public LinkedHashSet<Eclass> changeToHashSet(Set set){
 		LinkedHashSet<Eclass> res=new LinkedHashSet<Eclass>();
 		Iterator<Eclass> it=set.iterator();
 		while(it.hasNext()){
 			res.add(it.next());
 		}
 		return res;
 	}
 
 }
