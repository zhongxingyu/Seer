 package no.runsafe.creativetoolbox;
 
 import no.runsafe.creativetoolbox.command.*;
 import no.runsafe.creativetoolbox.command.Member.BlacklistCommand;
 import no.runsafe.creativetoolbox.command.Member.RemoveCommand;
 import no.runsafe.creativetoolbox.command.Member.WhitelistCommand;
 import no.runsafe.creativetoolbox.command.Tag.ClearCommand;
 import no.runsafe.creativetoolbox.command.Tag.FindCommand;
 import no.runsafe.creativetoolbox.command.Tag.SetCommand;
 import no.runsafe.creativetoolbox.database.*;
 import no.runsafe.creativetoolbox.event.CustomEvents;
 import no.runsafe.creativetoolbox.event.InteractEvents;
 import no.runsafe.creativetoolbox.event.SyncInteractEvents;
 import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.framework.api.IOutput;
 import no.runsafe.framework.api.command.Command;
 import no.runsafe.worldeditbridge.WorldEditInterface;
 import no.runsafe.worldgenerator.PlotChunkGenerator;
 import no.runsafe.worldguardbridge.WorldGuardInterface;
 
 public class Plugin extends RunsafeConfigurablePlugin
 {
 	@Override
 	protected void PluginSetup()
 	{
 		addComponent(getFirstPluginAPI(WorldGuardInterface.class));
 		addComponent(getFirstPluginAPI(WorldEditInterface.class));
 		addComponent(getFirstPluginAPI(PlotChunkGenerator.class));
 		addComponent(PlotMemberRepository.class);
 		addComponent(PlotLogRepository.class);
 		addComponent(PlotFilter.class);
 		addComponent(PlotManager.class);
 		addComponent(PlotList.class);
 		addComponent(ApprovedPlotRepository.class);
 		addComponent(PlotVoteRepository.class);
 		addComponent(PlotEntranceRepository.class);
 		addComponent(PlotTagRepository.class);
 		addComponent(PlotMemberBlacklistRepository.class);
 		addComponent(InteractEvents.class);
 		addComponent(SyncInteractEvents.class);
 		addComponent(PlotCalculator.class);
 		addComponent(Importers.class);
 		addComponent(PlotArgument.class);
 		addComponent(CustomEvents.class);
 
 		Command toolbox = new Command("creativetoolbox", "A collection of tools for use in a minecraft creative world.", null);
 		addComponent(toolbox);
 
 		Command member = new Command("member", "Tools to handle plot membership", null);
 		member.addSubCommand(getInstance(no.runsafe.creativetoolbox.command.Member.AddCommand.class));
 		member.addSubCommand(getInstance(RemoveCommand.class));
 		member.addSubCommand(getInstance(BlacklistCommand.class));
 		member.addSubCommand(getInstance(WhitelistCommand.class));
 		toolbox.addSubCommand(member);
 
 		Command tag = new Command("tag", "Plot tagging tool", null);
 		tag.addSubCommand(getInstance(no.runsafe.creativetoolbox.command.Tag.AddCommand.class));
 		tag.addSubCommand(getInstance(ClearCommand.class));
 		tag.addSubCommand(getInstance(FindCommand.class));
 		tag.addSubCommand(getInstance(SetCommand.class));
 		toolbox.addSubCommand(tag);
 
 		toolbox.addSubCommand(getInstance(OldPlotsCommand.class));
 		toolbox.addSubCommand(getInstance(ApprovePlotCommand.class));
 		toolbox.addSubCommand(getInstance(UnApprovePlotCommand.class));
 		toolbox.addSubCommand(getInstance(CheckApprovalCommand.class));
 		toolbox.addSubCommand(getInstance(SetEntranceCommand.class));
 		toolbox.addSubCommand(getInstance(TeleportCommand.class));
 		toolbox.addSubCommand(getInstance(ScanCommand.class));
 		toolbox.addSubCommand(getInstance(CleanCommand.class));
 		toolbox.addSubCommand(getInstance(ListCommand.class));
 		toolbox.addSubCommand(getInstance(RandomPlotCommand.class));
 		toolbox.addSubCommand(getInstance(DeleteHereCommand.class));
 		toolbox.addSubCommand(getInstance(FindFreePlotCommand.class));
 		toolbox.addSubCommand(getInstance(SelectCommand.class));
 		toolbox.addSubCommand(getInstance(RegenerateCommand.class));
 		toolbox.addSubCommand(getInstance(VoteCommand.class));
 		toolbox.addSubCommand(getInstance(ClaimCommand.class));
 		toolbox.addSubCommand(getInstance(ExtendCommand.class));
 		toolbox.addSubCommand(getInstance(GriefCleanupCommand.class));
 		toolbox.addSubCommand(getInstance(NextCommand.class));
 		toolbox.addSubCommand(getInstance(PreviousCommand.class));
 		toolbox.addSubCommand(getInstance(JumpCommand.class));
 	}
 }
