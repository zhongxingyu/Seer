 package org.eclipse.jst.jsf.apache.trinidad.tagsupport.el;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jst.jsf.context.symbol.ERuntimeSource;
 import org.eclipse.jst.jsf.context.symbol.ISymbol;
 import org.eclipse.jst.jsf.designtime.context.DTFacesContext;
 import org.eclipse.jst.jsf.designtime.el.AbstractDTVariableResolver;
 import org.eclipse.jst.jsf.designtime.el.IDecorativeResolver;
 import org.eclipse.jst.jsf.designtime.symbols.JSFSymbolFactory;
 
 /**
  * A decorative dt variable resolver that shadows Trinidad's runtime resolver.
  * 
  * @author cbateman
  * 
  */
 public class TrinidadDecorativeVariableResolver extends
         AbstractDTVariableResolver implements IDecorativeResolver
 {
     private final static JSFSymbolFactory _symbolFactory                = new JSFSymbolFactory();
 
     public final static String            PAGE_FLOW_SCOPE_VARIABLE_NAME = "pageFlowScope";
    public final static String            PAGE_FLOW_SCOPE_CLASS_NAME    = "org.apache.myfaces.trinidad.context.RequestContext";
     public final static String            PROCESS_SCOPE_VARIABLE_NAME   = "processScope";
 
     @Override
     public ISymbol[] getAllVariables(final DTFacesContext facesContext,
             final IAdaptable externalContextKey)
     {
         final Map<String, ISymbol> symbols = createSymbols(facesContext);
         return symbols.values().toArray(ISymbol.EMPTY_SYMBOL_ARRAY);
     }
 
     @Override
     public ISymbol resolveVariable(final DTFacesContext context,
             final String name, final IAdaptable externalContextKey)
     {
         return createSymbols(context).get(name);
     }
 
     private Map<String, ISymbol> createSymbols(final DTFacesContext context)
     {
         final IResource res = context.adaptContextObject();
 
         if (res != null)
         {
             final IProject project = res.getProject();
             if (project != null)
             {
                 final Map<String, ISymbol> symbols = new HashMap<String, ISymbol>();
 
                final ISymbol pageFlowVar = _symbolFactory
                         .createBeanOrUnknownInstanceSymbol(project,
                                PAGE_FLOW_SCOPE_CLASS_NAME,
                                 PAGE_FLOW_SCOPE_VARIABLE_NAME,
                                 ERuntimeSource.OTHER_LITERAL);
                symbols.put(pageFlowVar.getName(), pageFlowVar);

                 // TODO: we may be able to populate this map
                final ISymbol processScopeVar = _symbolFactory
                         .createUnknownInstanceSymbol(
                                PROCESS_SCOPE_VARIABLE_NAME,
                                 ERuntimeSource.OTHER_LITERAL);
                symbols.put(processScopeVar.getName(), processScopeVar);
                 return symbols;
             }
         }
         return Collections.emptyMap(); 
     }
 }
