 package com.pingpong.core;
 
 import com.pingpong.core.bo.PlayerAccountBO;
 import com.pingpong.core.bo.PlayerBO;
 import com.pingpong.domain.Player;
 import com.pingpong.domain.PlayerAccount;
 import com.pingpong.shared.AppService;
 import com.pingpong.shared.registration.PlayerRegistrationData;
 import net.sf.oval.constraint.NotNull;
 import net.sf.oval.guard.Guarded;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
 import java.util.List;
 
 /**
  * @author Artur Zhurat
  * @version 3.0
  * @since 25/01/2012
  */
 @Guarded
@Service
 public class AppServiceImpl implements AppService {
 	@Autowired
 	private PlayerBO playerBO;
 	@Autowired
 	private PlayerAccountBO playerAccountBO;
 
 	@Override
 	public List<Player> listPlayers() {
 		return playerBO.list();
 	}
 
 	@Override
 	public Integer insertPlayer(@NotNull Player player) {
 		return playerBO.insert(player);
 	}
 
 	@Override
 	public void register(@NotNull PlayerRegistrationData registrationData) {
 		playerBO.register(registrationData);
 	}
 
 	@Override
 	public PlayerAccount getAccountByEmail(@NotNull String email) {
 		return playerAccountBO.getByEmail(email);
 	}
 }
