 package org.dawnsci.macro.console;
 
 import org.dawnsci.macro.Activator;
 import org.eclipse.dawnsci.macro.api.IMacroService;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsolePageParticipant;
 import org.eclipse.ui.part.IPageBookViewPage;
 import org.python.pydev.core.IInterpreterInfo;
 import org.python.pydev.debug.newconsole.PydevConsole;
 
 public class ConsoleParticipant implements IConsolePageParticipant {
 	
 	private PydevConsole console;
 	private DocumentInserter inserter;
 
 	@Override
 	public void init(IPageBookViewPage page, IConsole c) {
 		
 		console = (PydevConsole)c;
 		
         Object oinfo = console.getInterpreterInfo();
         if (!(oinfo instanceof IInterpreterInfo)) return;
         
         IInterpreterInfo info = (IInterpreterInfo)oinfo;
         int type = info.getInterpreterType(); 
 
         this.inserter = new DocumentInserter();
         inserter.init(console.getViewer(), InsertionType.forPydevCode(type));
 
  
 		final IAction recordMacro = new Action("Record Macro", IAction.AS_CHECK_BOX) {
 			public void run() {
 				if (isChecked()) {
 					inserter.connect();
 				} else {
 					inserter.disconnect();
 				}
 			}
 		};
 		recordMacro.setImageDescriptor(Activator.getImage("icons/recordMacro.png"));
 		
 		page.getSite().getActionBars().getToolBarManager().add(new Separator());
 		page.getSite().getActionBars().getToolBarManager().add(recordMacro);
 	}
 
 	@Override
 	public Object getAdapter(Class adapter) {
 		if (IMacroService.class == adapter) return inserter.getMacroService();
 		return null;
 	}
 
 	@Override
 	public void dispose() {
		if (inserter!=null) inserter.disconnect();
 	}
 
 	@Override
 	public void activated() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void deactivated() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
