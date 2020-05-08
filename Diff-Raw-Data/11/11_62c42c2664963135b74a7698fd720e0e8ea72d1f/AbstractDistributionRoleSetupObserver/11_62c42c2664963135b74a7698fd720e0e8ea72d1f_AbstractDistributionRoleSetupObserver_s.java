 package deus.core.soul.common;
 
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
import deus.gatekeeper.rolesetup.DistributionRoleSetup;
import deus.gatekeeper.rolesetup.DistributionRoleSetupObserver;
 import deus.model.user.DistributionRole;
 import deus.model.user.id.UserId;
 
 // FIXME: move this to gatekeeper bundle
 public abstract class AbstractDistributionRoleSetupObserver implements DistributionRoleSetupObserver {
 
 	@Autowired
 	private DistributionRoleSetup distributionRoleSetup;
 
 
 	public AbstractDistributionRoleSetupObserver() {
 		super();
 	}
 
 
 	@PostConstruct
 	public void addObserver() {
 		distributionRoleSetup.addRoleSetupObserver(getUserRole(), this);
 	}
 
 
 	@PreDestroy
 	public void removeObserver() {
 		distributionRoleSetup.removeRoleSetupObserver(getUserRole(), this);
 	}
 
 
 	@Override
 	public void setUpRole(DistributionRole distributionRole, UserId userId) {
 		if (!distributionRole.equals(getUserRole()))
 			throw new RuntimeException("received notification for set up of user role " + distributionRole
 					+ " while only listening for set up of role " + getUserRole());
 		setUpRole(userId);
 	}
 
 
 	protected abstract void setUpRole(UserId userId);
 
 
 	@Override
 	public void tearDownRole(DistributionRole distributionRole, UserId userId) {
 		if (!distributionRole.equals(getUserRole()))
 			throw new RuntimeException("received notification for tear down of user role " + distributionRole
 					+ " while only listening for tear down of role " + getUserRole());
 		
 		tearDownRole(userId);
 	}
 
 
 	protected abstract void tearDownRole(UserId userId);
 
 
 	/**
 	 * Returns the user role, this observer should listen to
 	 * 
 	 * @return
 	 */
 	protected abstract DistributionRole getUserRole();
 
 }
