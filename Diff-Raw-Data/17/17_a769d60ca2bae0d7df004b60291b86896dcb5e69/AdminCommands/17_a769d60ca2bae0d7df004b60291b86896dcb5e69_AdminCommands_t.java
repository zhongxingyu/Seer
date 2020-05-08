 package com.volumetricpixels.vitals.main.commands;
 
 import com.google.common.base.Joiner;
 import org.apache.commons.lang3.StringUtils;
 
 import org.spout.api.chat.style.ChatStyle;
 import org.spout.api.command.CommandContext;
 import org.spout.api.command.CommandSource;
 import org.spout.api.command.annotated.Command;
 import org.spout.api.command.annotated.CommandPermissions;
 import org.spout.api.entity.Player;
 import org.spout.api.exception.CommandException;
 import org.spout.vanilla.data.Weather;
 import org.spout.vanilla.entity.VanillaController;
 import org.spout.vanilla.entity.world.VanillaSky;
 
 /**
  * Commands for admins.
  */
 public class AdminCommands {
 
	@Command(aliases = {"weather"}, usage = "<weather type>", desc = "Change the weather.", min = 1, max = 1)
 	@CommandPermissions("vitals.weather")
 	public void weather(CommandContext args, CommandSource source) throws CommandException {
 		if (source instanceof Player) {
			//Getting the sky
 			VanillaSky sky = VanillaSky.getSky(((Player) source).getWorld());
 
 			// Get the weather
 			String wName = args.getString(0, Weather.CLEAR.name());
 			Weather w = Weather.valueOf(wName.toUpperCase());
 			if (w == null) {
				source.sendMessage(ChatStyle.GRAY, "Unknown weather type '" + wName + "'. Available weather types: " + ChatStyle.RED, Joiner.on(' ').join(Weather.values()));
 				return;
 			}
 
 			sky.setWeather(w);
			source.sendMessage(ChatStyle.GRAY, "Weather set to '" + ChatStyle.RED, StringUtils.capitalize(w.toString()) + "'.");
 		}
 	}
	
	@Command(aliases = {"time"}, usage = "<time>", desc = "Change the time.", min = 1, max =1)
	@CommandPermissions("vitals.time")
	public void time(CommandContext args, CommandSource source) throws CommandException {
		//TODO: Finish the time command.
	}
 }
