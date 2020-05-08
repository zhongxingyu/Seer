 /*****************************************************************************
  * This source file is part of SBS (Screen Build System),                    *
  * which is a component of Screen Framework                                  *
  *                                                                           *
  * Copyright (c) 2008-2011 Ratouit Thomas                                    *
  *                                                                           *
  * This program is free software; you can redistribute it and/or modify it   *
  * under the terms of the GNU Lesser General Public License as published by  *
  * the Free Software Foundation; either version 3 of the License, or (at     *
  * your option) any later version.                                           *
  *                                                                           *
  * This program is distributed in the hope that it will be useful, but       *
  * WITHOUT ANY WARRANTY; without even the implied warranty of                *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser   *
  * General Public License for more details.                                  *
  *                                                                           *
  * You should have received a copy of the GNU Lesser General Public License  *
  * along with this program; if not, write to the Free Software Foundation,   *
  * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA, or go to   *
  * http://www.gnu.org/copyleft/lesser.txt.                                   *
  *****************************************************************************/
 
 package screen.tools.sbs.targets.defaults;
 
 import screen.tools.sbs.actions.ActionManager;
 import screen.tools.sbs.actions.defaults.ActionConfigurationLoad;
 import screen.tools.sbs.actions.defaults.ActionRepositoryDisplay;
 import screen.tools.sbs.actions.defaults.ActionRepositoryFilterDisplay;
 import screen.tools.sbs.context.ContextHandler;
 import screen.tools.sbs.context.defaults.ContextKeys;
 import screen.tools.sbs.context.defaults.RepositoryContext;
import screen.tools.sbs.context.defaults.SbsFileAndPathContext;
 import screen.tools.sbs.targets.Parameters;
 import screen.tools.sbs.targets.Target;
 import screen.tools.sbs.targets.TargetCall;
 import screen.tools.sbs.utils.Logger;
 import screen.tools.sbs.utils.TargetHelper;
import screen.tools.sbs.utils.targethelper.MandatoryPath;
 import screen.tools.sbs.utils.targethelper.MandatorySubTarget;
import screen.tools.sbs.utils.targethelper.OptionFile;
 import screen.tools.sbs.utils.targethelper.OptionVoid;
 
 public class TargetRepositories implements Target {
 	private TargetHelper helper;
 	private TargetHelper displayHelper;
 	private TargetHelper findHelper;
 	
	private MandatoryPath mandatoryPath;
 	private MandatorySubTarget mandatorySubTarget;
 	private OptionVoid optionVoid;
 	
 	public TargetRepositories() {
 		helper = new TargetHelper(getTargetCall());
 		displayHelper = new TargetHelper(getTargetCall());
 		findHelper = new TargetHelper(getTargetCall());
 		
		mandatoryPath = new MandatoryPath();
 		mandatorySubTarget = new MandatorySubTarget();
		
 		optionVoid = new OptionVoid();
 		
		helper.addMandatory(mandatoryPath);
 		helper.addMandatory(mandatorySubTarget);
 		helper.addOption(optionVoid);
 
		displayHelper.addMandatory(mandatoryPath);
 		displayHelper.addMandatory(mandatorySubTarget);
 		
		findHelper.addMandatory(mandatoryPath);
 		findHelper.addMandatory(mandatorySubTarget);
 	}
 	
 	public void registerActions(ActionManager actionManager,
 			Parameters parameters) {		
 		helper.perform(parameters);
 		
		SbsFileAndPathContext context = new SbsFileAndPathContext();
		context.setSbsXmlPath(mandatoryPath.getPath());

		
 		ContextHandler contextHandler = new ContextHandler();
 		contextHandler.addContext(ContextKeys.REPOSITORIES, new RepositoryContext());
		contextHandler.addContext(ContextKeys.SBS_FILE_AND_PATH, context);
 		actionManager.setContext(contextHandler);
 		
 		actionManager.pushAction(new ActionConfigurationLoad());
 		if("display".equals(mandatorySubTarget.getSubTarget())){
 			displayHelper.perform(parameters);
 			actionManager.pushAction(new ActionRepositoryDisplay());
 			actionManager.pushAction(new ActionRepositoryFilterDisplay());
 		}
 		if("find".equals(mandatorySubTarget.getSubTarget())){
 			findHelper.perform(parameters);
 		}
 	}
 
 	public TargetCall getTargetCall() {
 		TargetCall call = new TargetCall();
 		call.setTarget("repository");
 		return call;
 	}
 
 	public void usage() {
 		Logger.info("perform actions and/or display repository list");
 		helper.usage();
 	}
 }
