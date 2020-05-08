 package no.runsafe.toybox.command;
 
 import no.runsafe.framework.api.command.player.PlayerCommand;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 import no.runsafe.toybox.horses.HorseSpawner;
 import no.runsafe.toybox.horses.SpawnableHorseType;
 import no.runsafe.toybox.horses.SpawnableHorseVariant;
 
 import java.util.HashMap;
 import java.util.Random;
 
 public class SpawnHorse extends PlayerCommand
 {
 	public SpawnHorse(HorseSpawner horseSpawner)
 	{
 		super("spawnhorse", "Spawns a horse", "runsafe.toybox.spawnmob", "count", "tame");
 		this.horseSpawner = horseSpawner;
 	}
 
 	@Override
 	public String OnExecute(RunsafePlayer executor, HashMap<String, String> parameters)
 	{
 		return null;
 	}
 
 	@Override
 	public String OnExecute(RunsafePlayer executor, HashMap<String, String> parameters, String[] arguments)
 	{
		SpawnableHorseType type = (arguments.length > 0 ? SpawnableHorseType.valueOf(arguments[0].toUpperCase()) : this.getRandomHorseType());
		SpawnableHorseVariant variant = (arguments.length > 1 ? SpawnableHorseVariant.valueOf(arguments[1].toUpperCase()) : this.getRandomHorseVariant());
 
		if (type == null)
			return "&cInvalid horse type.";
 
		if (variant == null)
			return "&cInvalid horse variant.";
 
		int count = Integer.valueOf(parameters.get("count"));
 
		for (int i = 0; i < count; ++i)
 		{
			this.horseSpawner.spawnHorse(executor.getLocation(), type, variant, parameters.get("tame").equals("1"));
 		}
 
 		return null;
 	}
 
 	public SpawnableHorseType getRandomHorseType()
 	{
 		SpawnableHorseType[] values = SpawnableHorseType.values();
 		return values[this.random.nextInt(values.length)];
 	}
 
 	public SpawnableHorseVariant getRandomHorseVariant()
 	{
 		SpawnableHorseVariant[] values = SpawnableHorseVariant.values();
 		return values[this.random.nextInt(values.length)];
 	}
 
 	private Random random = new Random();
 	private HorseSpawner horseSpawner;
 }
