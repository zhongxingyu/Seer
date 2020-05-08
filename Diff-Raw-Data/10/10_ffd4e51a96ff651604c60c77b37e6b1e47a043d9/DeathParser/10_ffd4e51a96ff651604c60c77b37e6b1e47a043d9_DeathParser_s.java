 package no.runsafe.nchat;
 
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.event.IConfigurationChanged;
 
 import java.util.Map;
 
 public class DeathParser implements IConfigurationChanged
 {
 	public Death getDeathType(String deathMessage)
 	{
 		for (Death death : Death.values())
 			if (deathMessage.contains(death.getDefaultMessage()))
 				return death;
 
 		return Death.UNKNOWN;
 	}
 
 	public String getInvolvedEntityName(String deathMessage, Death death)
 	{
		String cause = deathMessage.substring(
			deathMessage.indexOf(death.getDefaultMessage()) + death.getDefaultMessage().length(),
			deathMessage.length()
 		).trim();
		if (cause.contains(" "))
			return cause.substring(0, cause.indexOf(" "));
		return cause;
 	}
 
 	public String isEntityName(String entityName)
 	{
 		for (EntityDeath entity : EntityDeath.values())
 		{
 			if (entity.getDeathName().equals(entityName))
 				return entity.name();
 		}
 		return null;
 	}
 
 	public String getCustomDeathMessage(String deathTag)
 	{
 		return (this.deathMessages.containsKey(deathTag)) ? this.deathMessages.get(deathTag) : null;
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration iConfiguration)
 	{
 		this.deathMessages = iConfiguration.getConfigValuesAsMap("deathMessages");
 	}
 
 	private Map<String, String> deathMessages;
 }
