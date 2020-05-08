 package br.zero.controlefinanceiro.commandlineparser;
 
 import br.zero.commandlineparser.CommandLineSwitch;
 import br.zero.commandlineparser.SubCommandLine;
 
 public class ModeloSwitches {
 
 	private ModeloCommand command;
 	private ModeloListSwitches listSwitches;
 	private ModeloAddSwitches addSwitches;
 	private ModeloSimulateSwitches simulateSwitches;
 	private ModeloRemoveSwitches removeSwitches;
 	private ModeloCloneSwitches cloneSwitches;
 
 	public ModeloCommand getCommand() {
 		return command;
 	}
 
 	@CommandLineSwitch(index = 1, parser = "ModeloCommandParser.parseComplexEnum", complexParser = true, subCommandLineProperties = {
 			@SubCommandLine(value = "LIST", subCommandLineClass = ModeloListSwitches.class, propertyName = "setListSwitches"),
 			@SubCommandLine(value = "ADD", subCommandLineClass = ModeloAddSwitches.class, propertyName = "setAddSwitches"),
 			@SubCommandLine(value = "SIMULATE", subCommandLineClass = ModeloSimulateSwitches.class, propertyName = "setSimulateSwitches"),
 			@SubCommandLine(value = "REMOVE", subCommandLineClass = ModeloRemoveSwitches.class, propertyName = "setRemoveSwitches"),
 			@SubCommandLine(value = "CLONE", subCommandLineClass = ModeloCloneSwitches.class, propertyName = "setCloneSwitches"), })
 	public void setCommand(ModeloCommand value) {
		value = command;
 	}
 
 	public ModeloListSwitches getListSwitches() {
 		return listSwitches;
 	}
 
 	public void setListSwitches(ModeloListSwitches value) {
 		listSwitches = value;
 	}
 
 	public ModeloAddSwitches getAddSwitches() {
 		return addSwitches;
 	}
 
 	public void setAddSwitches(ModeloAddSwitches value) {
 		addSwitches = value;
 	}
 
 	public ModeloSimulateSwitches getSimulateSwitches() {
 		return simulateSwitches;
 	}
 
 	public void setSimulateSwitches(ModeloSimulateSwitches value) {
 		simulateSwitches = value;
 	}
 
 	public ModeloRemoveSwitches getRemoveSwitches() {
 		return removeSwitches;
 	}
 
 	public void setRemoveSwitches(ModeloRemoveSwitches value) {
 		removeSwitches = value;
 	}
 
 	public ModeloCloneSwitches getCloneSwitches() {
 		return cloneSwitches;
 	}
 
 	public void setCloneSwitches(ModeloCloneSwitches value) {
 		cloneSwitches = value;
 	}
 }
