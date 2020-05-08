 package com.laboki.eclipse.plugin.jcolon.inserter;
 
 import java.util.Map;
 
 import lombok.ToString;
 
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.IPartService;
 import org.eclipse.ui.IWorkbenchPart;
 
 import com.google.common.collect.Maps;
 import com.laboki.eclipse.plugin.jcolon.Instance;
 
 @ToString
 public enum Factory implements Instance {
 	INSTANCE;
 
	private static final Map<IEditorPart, Instance> SERVICES_MAP = Maps.newHashMap();
 	private static final IPartService PART_SERVICE = EditorContext.getPartService();
 	private static final PartListener PART_LISTENER = new PartListener();
 
 	private static final class PartListener implements IPartListener {
 
 		public PartListener() {}
 
 		@Override
 		public void partActivated(final IWorkbenchPart part) {
 			Factory.enableAutomaticInserterFor(part);
 		}
 
 		@Override
		public void partClosed(final IWorkbenchPart part) {
			Factory.stopInserterService(part);
		}
 
 		@Override
 		public void partBroughtToTop(final IWorkbenchPart part) {}
 
 		@Override
		public void partDeactivated(final IWorkbenchPart part) {
			Factory.stopInserterService(part);
		}
 
 		@Override
 		public void partOpened(final IWorkbenchPart part) {}
 	}
 
 	private static void enableAutomaticInserterFor(final IWorkbenchPart part) {
 		if (Factory.isInvalidPart(part)) return;
 		Factory.startInserterService(part);
 	}
 
 	private static boolean isInvalidPart(final IWorkbenchPart part) {
 		return !Factory.isValidPart(part);
 	}
 
 	private static boolean isValidPart(final IWorkbenchPart part) {
 		if (Factory.isNotEditorPart(part)) return false;
 		if (EditorContext.isNotAJavaEditor((IEditorPart) part)) return false;
 		return true;
 	}
 
 	private static boolean isNotEditorPart(final IWorkbenchPart part) {
 		return !Factory.isEditorPart(part);
 	}
 
 	private static boolean isEditorPart(final IWorkbenchPart part) {
 		return part instanceof IEditorPart;
 	}
 
 	private static void startInserterService(final IWorkbenchPart part) {
 		Factory.stopAllInserterServices();
		Factory.SERVICES_MAP.put((IEditorPart) part, new SemiColonInserterServices().begin());
 	}
 
 	private static void stopAllInserterServices() {
		for (final IEditorPart part : Factory.SERVICES_MAP.keySet())
 			Factory.stopInserterService(part);
 	}
 
 	private static void stopInserterService(final IWorkbenchPart part) {
		if (Factory.servicesMapDoesNotContain(part)) return;
		Factory.SERVICES_MAP.get(part).end();
		Factory.SERVICES_MAP.remove(part);
	}

	private static boolean servicesMapDoesNotContain(final IWorkbenchPart part) {
		return !Factory.SERVICES_MAP.containsKey(part);
 	}
 
 	@Override
 	public Instance begin() {
 		Factory.enableAutomaticInserterFor(Factory.PART_SERVICE.getActivePart());
 		Factory.PART_SERVICE.addPartListener(Factory.PART_LISTENER);
 		return this;
 	}
 
 	@Override
 	public Instance end() {
 		Factory.PART_SERVICE.removePartListener(Factory.PART_LISTENER);
 		Factory.stopAllInserterServices();
 		return this;
 	}
 }
