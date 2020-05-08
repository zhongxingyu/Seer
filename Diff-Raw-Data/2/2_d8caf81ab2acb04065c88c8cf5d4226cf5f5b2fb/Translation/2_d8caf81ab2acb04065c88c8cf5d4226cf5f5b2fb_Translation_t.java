 package multiworld.translation;
 
 import multiworld.translation.message.PackedMessageData;
 
 /**
  *
  * @author ferrybig
  */
 public enum Translation implements PackedMessageData
 {
 	LACKING_PERMISSIONS("You don't have the required permissions!"),
 	COMMAND_NOT_FOUND("Unknown command! Do \"/mw help\" for a list of commands."),
 	WORLD_NOT_FOUND("The specified world '%world%' isn't found!"),
 	PLAYER_NOT_FOUND("The specified player %player% is not found"),
 	INVALID_LOCATION("The specified loaction is invalid!"),
 	INVALID_WORLD("The specified world %world% doesn't have a valid syntax"),
 	INVALID_FLAG("The specified flag is invalid"),
 	INVALID_FLAG_VALUE("The specified flag value is invalid"),
 	NEED_PLAYER("This command can't be executed from the console"),
 	WORLD_UNLOADED_ALREADY("World %world% is already unloaded!"),
 	WORLD_UNLOADING_START("The unloading of the world %world% is started!"),
 	WORLD_UNLOADING_END("The unloading of the world %world% is ended succesfully!"),
 	WORLD_UNLOADING_FAILED("The unloading of the world %world% is failed, you can't unload the main world and worlds with players!"),
 	WORLD_LOADED_ALREADY("World %world% is already loaded, no need to load it again!"),
	WORLD_LOADING_START("Starting to load world %world%, expect some lagg!"),
 	WORLD_LOADING_END("World %world% loaded succesfully!"),
 	WORLD_LOADING_FAILED("Failed to load world %world%!"),
 	PREPARING_SPAWN_ARENA("Loading %world%: %percent%%"),
 	COMMAND_LIST_HEADER("The following world are found by MultiWorld:"),
 	COMMAND_LIST_DATA("%world%: %loaded% - %type%"),
 	COMMAND_INFO_DATA("We know the following about your world:\n"
 		+ "World Name: %world%\n"
 		+ "World Seed: %seed%\n"
 		+ "Generator: %generator%:%options%\n"
 		+ "Environment: %env%\n"
 		+ "Difficulty: %difficulty%"),
 	COMMAND_DELETE_START("Starting to remove %world% from the multiworld database"),
 	COMMAND_DELETE_SUCCESS("World %world% has been deleted from the multiworld database."),
 	COMMAND_DELETE_FAILED("Failed to delete world %world%"),
 	COMMAND_CREATE_WORLD_EXISTS("%world% is alread existing, mayby you need to load it?"),
 	COMMAND_CREATE_START("Creating new world %world%. Generator: %generator%:%option%. Seed: %seed%"),
 	COMMAND_CREATE_GET_PRE_ERROR("Failed to create a world with the name %world%"),
 	COMMAND_CREATE_GET_ERROR("Generator returned the following error:\n%error%"),
 	COMMAND_CREATE_SUCCES("Succesfully created world %world%, now you only need to do /mw load %world%"),
 	COMMAND_SPAWN_SUCCES("You have been warped to the spawn of this world!"),
 	COMMAND_SPAWN_FAIL_CONSOLE("Sorry, I can't warp you, #console"),
 	COMMAND_SETSPAWN_FAIL_CONSOLE("Sorry, I can't get your actual location"),
 	COMMAND_SETSPAWN_SUCCESS("Configured spawn location succesfully to %x% %y% %z% at %world%"),
 	COMMAND_SETSPAWN_FAIL("Failed to set our spawn, maybe a other plugin blocked our request!"),
 	COMMAND_LINK_SET_LINK("Created %portal% link from %world% to %target%!"),
 	COMMAND_LINK_REMOVE_LINK("Removed %portal% link from %world%"),
 	COMMAND_LINK_PORTALHANDLER_NOT_FOUND("Unable to acces portal handler addon, is it enabled inside the configuration?"),
 	COMMAND_HELP_TURNED_OFF("Sorry, help is turned off by the administrators"),
 	COMMAND_MOVE_MESSAGE_SUCCES("Moved player %player% to world %world%"),
 	COMMAND_SETFLAG_SUCCES("Succesfully set flag %flag% to %value%"),
 	COMMAND_SETFLAG_FAIL_SAME_VALUE("flag %flag% is already set to this value"),
 	COMMAND_GETFLAG_SUCCESS("The following flags are set at this world:"),
 	COMMAND_SAVE_SUCCES("Succesfully saves multiworld data!"),
 	COMMAND_SAVE_FAIL("Unable to save multiworld data to disk! See console for details!"),
 	COMMAND_RELOAD_SUCCES("Reloaded multiworld configuration"),
 	COMMAND_RELOAD_FAIL("Failed to reload multiworld configuration! See console for details"),
 	MULTIWORLD_SAVE_SUCCES("Saved automaticly"),
 	MULTIWORLD_SAVE_FAIL_RETRY("Saved automaticly FAILED! Trying again in 10 seconds!"),
 	MULTIWORLD_SAVE_FAIL_RETRY_DIRECT("Saved automaticly FAILED! Trying again...."),
 	MULTIWORLD_SAVE_FAIL_SHUTDOWN("Wasn't able to save data! Data loss has been occured!!!"),
 	MULTIWORLD_SAVE_FAIL("Saved automaticly FAILED! Check console for details!"),
 	;
 	private final String humanText;
 
 	private Translation(String humanText)
 	{
 		this.humanText = humanText;
 	}
 
 	@Override
 	public String getBinary()
 	{
 		return this.name();
 	}
 
 	public String getHumanText()
 	{
 		return humanText;
 	}
 
 	/**
 	 *
 	 * @param prevFormat the value of prevFormat
 	 * @return
 	 */
 	@Override
 	public String transformMessage(String prevFormat)
 	{
 		return this.humanText;
 	}
 }
