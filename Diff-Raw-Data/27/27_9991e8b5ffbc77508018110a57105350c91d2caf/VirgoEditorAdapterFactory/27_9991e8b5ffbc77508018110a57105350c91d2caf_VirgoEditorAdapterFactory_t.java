 /**
  * <copyright>
  *
  * TODO Copyright
  *
  * </copyright>
  *
  */
 package org.eclipse.virgo.ide.runtime.internal.ui.editor;
 
import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import org.eclipse.core.runtime.IAdapterFactory;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.part.MultiPageEditorPart;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiPlugin;
 import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
 import org.eclipse.wst.server.ui.editor.ServerEditorPart;
 import org.eclipse.wst.server.ui.internal.editor.ServerEditor;
 
 /**
  * @author Miles Parker
  */
 public class VirgoEditorAdapterFactory implements IAdapterFactory {
 
 	/**
 	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
 	 */
 	public Object getAdapter(Object adaptableObject, Class adapterType) {
 		if (adapterType == IContentOutlinePage.class && adaptableObject instanceof ServerEditor) {
 			if (getVirgoServer((IEditorPart) adaptableObject) != null) {
 				return new ServerOutlinePage((ServerEditor) adaptableObject);
 			}
 		}
 		return null;
 	}
 
 	public static IServer getVirgoServer(IEditorPart part) {
 		IServer server = getServer(part);
 		if (server != null && server.getServerType().getId().equals(ServerUiPlugin.VIRGO_SERVER_ID)) {
 			return server;
 		}
 		return null;
 	}
 
 	public static IServer getServer(IEditorPart part) {
 		if (part instanceof ServerEditor) {
			Method method;
 			try {
				method = MultiPageEditorPart.class.getDeclaredMethod("getActiveEditor", new Class[] {});
 				method.setAccessible(true);
 				Object result = method.invoke(part, new Object[] {});
 				if (result instanceof ServerEditorPart) {
					IServerWorkingCopy serverEditor = ((ServerEditorPart) result).getServer();
					if (serverEditor != null) {
						IServer server = serverEditor.getOriginal();
						return server;
					}
 				}
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
 	 */
 	public Class[] getAdapterList() {
 		return new Class[] { IContentOutlinePage.class };
 	}
 
 }
