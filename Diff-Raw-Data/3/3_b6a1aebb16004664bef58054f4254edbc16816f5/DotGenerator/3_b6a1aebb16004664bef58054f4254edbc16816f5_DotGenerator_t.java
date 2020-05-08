 package de.protos.FlowOfWork.core.fow.generator;
 
 import com.google.common.base.Objects;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import de.protos.FlowOfWork.core.fow.foW.Activity;
 import de.protos.FlowOfWork.core.fow.foW.ActivityRef;
 import de.protos.FlowOfWork.core.fow.foW.Decision;
 import de.protos.FlowOfWork.core.fow.foW.DecisionTransition;
 import de.protos.FlowOfWork.core.fow.foW.FinalTransition;
 import de.protos.FlowOfWork.core.fow.foW.InitialTransition;
 import de.protos.FlowOfWork.core.fow.foW.Model;
 import de.protos.FlowOfWork.core.fow.foW.Node;
 import de.protos.FlowOfWork.core.fow.foW.NonInitialTransition;
 import de.protos.FlowOfWork.core.fow.foW.Port;
 import de.protos.FlowOfWork.core.fow.foW.State;
 import de.protos.FlowOfWork.core.fow.foW.Step;
 import de.protos.FlowOfWork.core.fow.foW.Transition;
 import de.protos.FlowOfWork.core.fow.foW.WorkProduct;
 import de.protos.FlowOfWork.core.fow.foW.WorkProductType;
 import de.protos.FlowOfWork.core.fow.generator.PathInfo;
 import de.protos.FlowOfWork.core.fow.generator.WorkProductState;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.xtend2.lib.StringConcatenation;
 import org.eclipse.xtext.generator.IFileSystemAccess;
 import org.eclipse.xtext.xbase.lib.CollectionLiterals;
 import org.eclipse.xtext.xbase.lib.IterableExtensions;
 import org.eclipse.xtext.xbase.lib.ObjectExtensions;
 import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
 
 @Singleton
 @SuppressWarnings("all")
 public class DotGenerator {
   @Inject
   private PathInfo pathInfo;
   
   public void generateDot(final Model model, final IFileSystemAccess fsa) {
     this.generateActivityDiagramsForAllActivities(model, fsa);
     this.generateAntBuildfile(fsa);
   }
   
   private void generateActivityDiagramsForAllActivities(final Model model, final IFileSystemAccess fsa) {
     EList<Activity> _activities = model.getActivities();
     for (final Activity activity : _activities) {
       {
         String _dotGenPath = this.pathInfo.getDotGenPath(activity);
         CharSequence _generateActivityDiagramForOneActivity = this.generateActivityDiagramForOneActivity(activity);
         fsa.generateFile(_dotGenPath, _generateActivityDiagramForOneActivity);
         boolean _hasBehavior = this.hasBehavior(activity);
         if (_hasBehavior) {
           String _dotActivityBehaviorPath = this.pathInfo.getDotActivityBehaviorPath(activity);
           CharSequence _generateActivityBehavior = this.generateActivityBehavior(activity);
           fsa.generateFile(_dotActivityBehaviorPath, _generateActivityBehavior);
         }
       }
     }
   }
   
   private CharSequence generateActivityDiagramForOneActivity(final Activity activity) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("digraph ");
     String _name = activity.getName();
     _builder.append(_name, "");
     _builder.append("{");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("rankdir=TD;");
     _builder.newLine();
     _builder.append("\t");
     CharSequence _generateActivityDiagram = this.generateActivityDiagram(activity);
     _builder.append(_generateActivityDiagram, "	");
     _builder.newLineIfNotEmpty();
     _builder.append("}");
     _builder.newLine();
     return _builder;
   }
   
   public String getLabel(final ActivityRef ar) {
     Activity _type = ar.getType();
     return _type.getName();
   }
   
   public String getLabel(final Port p) {
     WorkProduct _type = p.getType();
     State _state = p.getState();
     WorkProductState _workProductState = new WorkProductState(_type, _state);
     final WorkProductState wps = _workProductState;
     final String idString = this.getLabel(wps);
     return idString;
   }
   
   public String getLabel(final WorkProductState wps) {
     String stateString = "";
     boolean _notEquals = (!Objects.equal(wps.state, null));
     if (_notEquals) {
       String _name = wps.state.getName();
       String _plus = ("(" + _name);
       String _plus_1 = (_plus + ")");
       stateString = _plus_1;
     }
     WorkProductType _type = wps.prod.getType();
     String _name_1 = _type.getName();
     String _plus_2 = ("\"" + _name_1);
     String _plus_3 = (_plus_2 + "::");
     String _name_2 = wps.prod.getName();
     String _plus_4 = (_plus_3 + _name_2);
     String _plus_5 = (_plus_4 + stateString);
     return (_plus_5 + "\"");
   }
   
   private CharSequence generateActivityDiagram(final Activity activity) {
     CharSequence _xblockexpression = null;
     {
       HashMap<WorkProductState,Port> _hashMap = new HashMap<WorkProductState,Port>();
       final Map<WorkProductState,Port> outPortMappings = _hashMap;
       HashMap<WorkProductState,List<ActivityRef>> _hashMap_1 = new HashMap<WorkProductState,List<ActivityRef>>();
       final HashMap<WorkProductState,List<ActivityRef>> inputSubMappings = _hashMap_1;
       HashMap<WorkProductState,List<ActivityRef>> _hashMap_2 = new HashMap<WorkProductState,List<ActivityRef>>();
       final HashMap<WorkProductState,List<ActivityRef>> outputSubMappings = _hashMap_2;
       EList<Port> _outPorts = activity.getOutPorts();
       final Procedure1<Port> _function = new Procedure1<Port>() {
           public void apply(final Port p) {
             WorkProduct _type = p.getType();
             State _state = p.getState();
             WorkProductState _workProductState = new WorkProductState(_type, _state);
             outPortMappings.put(_workProductState, p);
           }
         };
       IterableExtensions.<Port>forEach(_outPorts, _function);
       EList<ActivityRef> _subActivities = activity.getSubActivities();
       final Procedure1<ActivityRef> _function_1 = new Procedure1<ActivityRef>() {
           public void apply(final ActivityRef a) {
             Activity _type = a.getType();
             EList<Port> _inPorts = _type.getInPorts();
             final Procedure1<Port> _function = new Procedure1<Port>() {
                 public void apply(final Port p) {
                   WorkProduct _type = p.getType();
                   State _state = p.getState();
                   WorkProductState _workProductState = new WorkProductState(_type, _state);
                   final WorkProductState id = _workProductState;
                   boolean _containsKey = inputSubMappings.containsKey(id);
                   if (_containsKey) {
                     List<ActivityRef> _get = inputSubMappings.get(id);
                     _get.add(a);
                   } else {
                     ArrayList<ActivityRef> _arrayList = new ArrayList<ActivityRef>();
                     final Procedure1<ArrayList<ActivityRef>> _function = new Procedure1<ArrayList<ActivityRef>>() {
                         public void apply(final ArrayList<ActivityRef> it) {
                           it.add(a);
                         }
                       };
                     ArrayList<ActivityRef> _doubleArrow = ObjectExtensions.<ArrayList<ActivityRef>>operator_doubleArrow(_arrayList, _function);
                     inputSubMappings.put(id, _doubleArrow);
                   }
                 }
               };
             IterableExtensions.<Port>forEach(_inPorts, _function);
             Activity _type_1 = a.getType();
             EList<Port> _outPorts = _type_1.getOutPorts();
             final Procedure1<Port> _function_1 = new Procedure1<Port>() {
                 public void apply(final Port p) {
                   WorkProduct _type = p.getType();
                   State _state = p.getState();
                   WorkProductState _workProductState = new WorkProductState(_type, _state);
                   final WorkProductState id = _workProductState;
                   boolean _containsKey = outputSubMappings.containsKey(id);
                   if (_containsKey) {
                     List<ActivityRef> _get = outputSubMappings.get(id);
                     _get.add(a);
                   } else {
                     ArrayList<ActivityRef> _arrayList = new ArrayList<ActivityRef>();
                     final Procedure1<ArrayList<ActivityRef>> _function = new Procedure1<ArrayList<ActivityRef>>() {
                         public void apply(final ArrayList<ActivityRef> it) {
                           it.add(a);
                         }
                       };
                     ArrayList<ActivityRef> _doubleArrow = ObjectExtensions.<ArrayList<ActivityRef>>operator_doubleArrow(_arrayList, _function);
                     outputSubMappings.put(id, _doubleArrow);
                   }
                 }
               };
             IterableExtensions.<Port>forEach(_outPorts, _function_1);
           }
         };
       IterableExtensions.<ActivityRef>forEach(_subActivities, _function_1);
       StringConcatenation _builder = new StringConcatenation();
       _builder.append("\t\t");
       _builder.append("subgraph cluster_inputs_");
       String _name = activity.getName();
       _builder.append(_name, "		");
       _builder.append(" {");
       _builder.newLineIfNotEmpty();
       _builder.append("\t");
       _builder.append("color=none");
       _builder.newLine();
       {
         EList<Port> _inPorts = activity.getInPorts();
         for(final Port p : _inPorts) {
           _builder.append("\t");
           String _label = this.getLabel(p);
           _builder.append(_label, "	");
           _builder.append(" [shape=rectangle,color=skyblue2,style=filled,forcelabels=true]");
           _builder.newLineIfNotEmpty();
         }
       }
       _builder.append("}");
       _builder.newLine();
       _builder.append("subgraph cluster_outputs_");
       String _name_1 = activity.getName();
       _builder.append(_name_1, "");
       _builder.append(" {");
       _builder.newLineIfNotEmpty();
       _builder.append("\t");
       _builder.append("color=none");
       _builder.newLine();
       {
         EList<Port> _outPorts_1 = activity.getOutPorts();
         for(final Port p_1 : _outPorts_1) {
           _builder.append("\t");
           String _label_1 = this.getLabel(p_1);
           _builder.append(_label_1, "	");
           _builder.append(" [shape=rectangle,color=skyblue2,style=filled,forcelabels=true]");
           _builder.newLineIfNotEmpty();
         }
       }
       _builder.append("}");
       _builder.newLine();
       _builder.newLine();
       {
         EList<ActivityRef> _subActivities_1 = activity.getSubActivities();
         boolean _isEmpty = _subActivities_1.isEmpty();
         if (_isEmpty) {
           String _name_2 = activity.getName();
           _builder.append(_name_2, "");
           _builder.append(" [shape=pentagon,color=sandybrown,style=filled, orientation=-90]");
           _builder.newLineIfNotEmpty();
         } else {
           _builder.append("subgraph cluster_subactivities_");
           String _name_3 = activity.getName();
           _builder.append(_name_3, "");
           _builder.append(" {");
           _builder.newLineIfNotEmpty();
           _builder.append("\t");
           _builder.append("label = \"");
           String _name_4 = activity.getName();
           _builder.append(_name_4, "	");
           _builder.append("\"");
           _builder.newLineIfNotEmpty();
           _builder.append("\t");
           _builder.append("color=black");
           _builder.newLine();
           {
             EList<ActivityRef> _subActivities_2 = activity.getSubActivities();
             for(final ActivityRef subactivity : _subActivities_2) {
               _builder.append("\t");
               String _label_2 = this.getLabel(subactivity);
               _builder.append(_label_2, "	");
               _builder.append(" [shape=pentagon,color=sandybrown,style=filled,orientation=-90]");
               _builder.newLineIfNotEmpty();
             }
           }
           _builder.append("\t");
           CharSequence _generateSubActivityEdges = this.generateSubActivityEdges(outPortMappings, inputSubMappings, outputSubMappings);
           _builder.append(_generateSubActivityEdges, "	");
           _builder.newLineIfNotEmpty();
           _builder.append("}");
           _builder.newLine();
         }
       }
       _builder.newLine();
       {
         EList<Port> _inPorts_1 = activity.getInPorts();
         for(final Port p_2 : _inPorts_1) {
           WorkProduct _type = p_2.getType();
           State _state = p_2.getState();
           WorkProductState _workProductState = new WorkProductState(_type, _state);
           final WorkProductState id = _workProductState;
           _builder.newLineIfNotEmpty();
           {
             EList<ActivityRef> _subActivities_3 = activity.getSubActivities();
             boolean _isEmpty_1 = _subActivities_3.isEmpty();
             if (_isEmpty_1) {
               String _label_3 = this.getLabel(id);
               _builder.append(_label_3, "");
               _builder.append(" -> ");
               String _name_5 = activity.getName();
               _builder.append(_name_5, "");
               _builder.newLineIfNotEmpty();
             } else {
               List<ActivityRef> _elvis = null;
               List<ActivityRef> _get = inputSubMappings.get(id);
               if (_get != null) {
                 _elvis = _get;
               } else {
                 List<ActivityRef> _emptyList = CollectionLiterals.<ActivityRef>emptyList();
                 _elvis = ObjectExtensions.<List<ActivityRef>>operator_elvis(_get, _emptyList);
               }
               final List<ActivityRef> subActRefs = _elvis;
               _builder.newLineIfNotEmpty();
               {
                 for(final ActivityRef ar : subActRefs) {
                   String _label_4 = this.getLabel(id);
                   _builder.append(_label_4, "");
                   _builder.append(" -> ");
                   String _label_5 = this.getLabel(ar);
                   _builder.append(_label_5, "");
                   _builder.newLineIfNotEmpty();
                 }
               }
             }
           }
         }
       }
       _builder.newLine();
       {
         EList<Port> _outPorts_2 = activity.getOutPorts();
         for(final Port p_3 : _outPorts_2) {
           WorkProduct _type_1 = p_3.getType();
           State _state_1 = p_3.getState();
           WorkProductState _workProductState_1 = new WorkProductState(_type_1, _state_1);
           final WorkProductState id_1 = _workProductState_1;
           _builder.newLineIfNotEmpty();
           {
             EList<ActivityRef> _subActivities_4 = activity.getSubActivities();
             boolean _isEmpty_2 = _subActivities_4.isEmpty();
             if (_isEmpty_2) {
               String _name_6 = activity.getName();
               _builder.append(_name_6, "");
               _builder.append(" -> ");
               String _label_6 = this.getLabel(id_1);
               _builder.append(_label_6, "");
               _builder.append(" ");
               _builder.newLineIfNotEmpty();
             } else {
               List<ActivityRef> _elvis_1 = null;
               List<ActivityRef> _get_1 = outputSubMappings.get(id_1);
               if (_get_1 != null) {
                 _elvis_1 = _get_1;
               } else {
                 List<ActivityRef> _emptyList_1 = CollectionLiterals.<ActivityRef>emptyList();
                 _elvis_1 = ObjectExtensions.<List<ActivityRef>>operator_elvis(_get_1, _emptyList_1);
               }
               final List<ActivityRef> subActRefs_1 = _elvis_1;
               _builder.newLineIfNotEmpty();
               {
                 for(final ActivityRef ar_1 : subActRefs_1) {
                   String _label_7 = this.getLabel(ar_1);
                   _builder.append(_label_7, "");
                   _builder.append(" -> ");
                   String _label_8 = this.getLabel(id_1);
                   _builder.append(_label_8, "");
                   _builder.newLineIfNotEmpty();
                 }
               }
             }
           }
         }
       }
       _xblockexpression = (_builder);
     }
     return _xblockexpression;
   }
   
   private CharSequence generateSubActivityEdges(final Map<WorkProductState,Port> outPortMappings, final Map<WorkProductState,List<ActivityRef>> inputSubMappings, final Map<WorkProductState,List<ActivityRef>> outputSubMappings) {
     StringConcatenation _builder = new StringConcatenation();
     {
       Set<WorkProductState> _keySet = outputSubMappings.keySet();
       for(final WorkProductState id : _keySet) {
         final List<ActivityRef> outActRefs = outputSubMappings.get(id);
         _builder.newLineIfNotEmpty();
         List<ActivityRef> _elvis = null;
         List<ActivityRef> _get = inputSubMappings.get(id);
         if (_get != null) {
           _elvis = _get;
         } else {
           List<ActivityRef> _emptyList = CollectionLiterals.<ActivityRef>emptyList();
           _elvis = ObjectExtensions.<List<ActivityRef>>operator_elvis(_get, _emptyList);
         }
         final List<ActivityRef> inActRefs = _elvis;
         _builder.newLineIfNotEmpty();
         {
           boolean _containsKey = outPortMappings.containsKey(id);
           boolean _not = (!_containsKey);
           if (_not) {
             String _label = this.getLabel(id);
             _builder.append(_label, "");
             _builder.append(" [shape=rectangle,color=skyblue2,style=filled,forcelabels=true]");
             _builder.newLineIfNotEmpty();
             {
               for(final ActivityRef src : outActRefs) {
                 String _label_1 = this.getLabel(src);
                 _builder.append(_label_1, "");
                 _builder.append(" -> ");
                 String _label_2 = this.getLabel(id);
                 _builder.append(_label_2, "");
                 _builder.newLineIfNotEmpty();
               }
             }
           }
         }
         {
           for(final ActivityRef tgt : inActRefs) {
             String _label_3 = this.getLabel(id);
             _builder.append(_label_3, "");
             _builder.append(" -> ");
             String _label_4 = this.getLabel(tgt);
             _builder.append(_label_4, "");
             _builder.newLineIfNotEmpty();
           }
         }
       }
     }
     return _builder;
   }
   
   private CharSequence generateActivityBehavior(final Activity activity) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("digraph ");
     String _name = activity.getName();
     _builder.append(_name, "");
     _builder.append("_Behavior{");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("rankdir=TD;");
     _builder.newLine();
     _builder.append("\t");
     EList<Step> _steps = activity.getSteps();
     CharSequence _generateAllSteps = this.generateAllSteps(_steps);
     _builder.append(_generateAllSteps, "	");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     EList<Decision> _decisions = activity.getDecisions();
     CharSequence _generateAllDecisions = this.generateAllDecisions(_decisions);
     _builder.append(_generateAllDecisions, "	");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     EList<Transition> _transitions = activity.getTransitions();
     CharSequence _generateAllTransitions = this.generateAllTransitions(_transitions);
     _builder.append(_generateAllTransitions, "	");
     _builder.newLineIfNotEmpty();
     _builder.append("}\t");
     _builder.newLine();
     return _builder;
   }
   
   public boolean hasBehavior(final Activity activity) {
     EList<Step> _steps = activity.getSteps();
     return this.emptyList(_steps);
   }
   
   public boolean emptyList(final EList<? extends Object> list) {
     boolean _equals = Objects.equal(list, null);
     if (_equals) {
       return false;
     } else {
       boolean _isEmpty = list.isEmpty();
       if (_isEmpty) {
         return false;
       } else {
         return true;
       }
     }
   }
   
   private CharSequence generateAllSteps(final EList<Step> steps) {
     StringConcatenation _builder = new StringConcatenation();
     {
       for(final Step step : steps) {
         String _name = step.getName();
         _builder.append(_name, "");
         _builder.append(" [shape=ellipse]");
         _builder.newLineIfNotEmpty();
       }
     }
     return _builder;
   }
   
   private CharSequence generateAllDecisions(final EList<Decision> decisions) {
     StringConcatenation _builder = new StringConcatenation();
     {
       for(final Decision decision : decisions) {
         String _name = decision.getName();
         _builder.append(_name, "");
         _builder.append(" [shape=diamond]");
         _builder.newLineIfNotEmpty();
       }
     }
     return _builder;
   }
   
   private CharSequence generateAllTransitions(final EList<Transition> transitions) {
     StringConcatenation _builder = new StringConcatenation();
     {
       for(final Transition transition : transitions) {
         CharSequence _generateTransition = this.generateTransition(transition);
         _builder.append(_generateTransition, "");
         _builder.newLineIfNotEmpty();
       }
     }
     return _builder;
   }
   
   private CharSequence _generateTransition(final InitialTransition transition) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("i [shape = circle, color=black, style=filled] ");
     _builder.newLine();
     _builder.append("i -> ");
     Node _to = transition.getTo();
     String _name = _to.getName();
     _builder.append(_name, "");
     _builder.newLineIfNotEmpty();
     return _builder;
   }
   
   private CharSequence _generateTransition(final FinalTransition transition) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("f [shape = doublecircle, color=black, style=filled] ");
     _builder.newLine();
     Node _from = transition.getFrom();
     String _name = _from.getName();
     _builder.append(_name, "");
     _builder.append(" -> f");
     _builder.newLineIfNotEmpty();
     return _builder;
   }
   
   private CharSequence _generateTransition(final NonInitialTransition transition) {
     StringConcatenation _builder = new StringConcatenation();
     Node _from = transition.getFrom();
     String _name = _from.getName();
     _builder.append(_name, "");
     _builder.append(" -> ");
     Node _to = transition.getTo();
     String _name_1 = _to.getName();
     _builder.append(_name_1, "");
     _builder.newLineIfNotEmpty();
     return _builder;
   }
   
   private CharSequence _generateTransition(final DecisionTransition transition) {
     StringConcatenation _builder = new StringConcatenation();
     Decision _from = transition.getFrom();
     String _name = _from.getName();
     _builder.append(_name, "");
     _builder.append(" -> ");
     Node _to = transition.getTo();
     String _name_1 = _to.getName();
     _builder.append(_name_1, "");
     _builder.append(" [label=\"[");
     String _guard = transition.getGuard();
     _builder.append(_guard, "");
     _builder.append("]\"]");
     _builder.newLineIfNotEmpty();
     return _builder;
   }
   
   private CharSequence generateDotBuildfileContent() {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
     _builder.newLine();
     _builder.append("<!-- ====================================================================== ");
     _builder.newLine();
     _builder.append("     ");
     _builder.append("Generates bitmap images from DOT files for process documentation");
     _builder.newLine();
     _builder.append("     ");
     _builder.append("====================================================================== -->");
     _builder.newLine();
     _builder.append("<project name=\"builddotfiles\" default=\"build\">");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<description>");
     _builder.newLine();
     _builder.append("    \t");
     _builder.append("Generates bitmap images from DOT files for process documentation");
     _builder.newLine();
     _builder.append("    ");
     _builder.append("</description>");
     _builder.newLine();
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<target name=\"build\" description=\"description\" depends=\"init\">");
     _builder.newLine();
     _builder.append("\t\t");
     _builder.append("<apply executable=\"${dot.binpath}/dot\">");
     _builder.newLine();
     _builder.append("\t\t\t");
     _builder.append("<arg value=\"-Tjpg\" />");
     _builder.newLine();
     _builder.append("\t\t\t");
     _builder.append("<srcfile />");
     _builder.newLine();
     _builder.append("\t\t\t");
     _builder.append("<arg value=\"-o\" />");
     _builder.newLine();
     _builder.append("\t\t\t");
     _builder.append("<targetfile />");
     _builder.newLine();
     _builder.append("\t\t\t");
     _builder.append("<fileset dir=\"./\" includes=\"*.dot\" />");
     _builder.newLine();
     _builder.append("\t\t\t");
     _builder.append("<mapper type=\"glob\" from=\"*.dot\" to=\"*.jpg\" />");
     _builder.newLine();
     _builder.append("\t\t");
     _builder.append("</apply>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("</target>");
     _builder.newLine();
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<target name=\"init\">");
     _builder.newLine();
     _builder.append("\t\t");
    _builder.append("<property environment=\"env\" />");
    _builder.newLine();
    _builder.append("\t\t");
     _builder.append("<property name=\"dot.binpath\" value=\"${env.DOT_PATH}\" />");
     _builder.newLine();
     _builder.append("\t\t");
     _builder.append("<property name=\"dot.sourcepath\" value=\"\" />");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("</target>");
     _builder.newLine();
     _builder.newLine();
     _builder.append("</project>");
     _builder.newLine();
     return _builder;
   }
   
   private void generateAntBuildfile(final IFileSystemAccess fsa) {
     CharSequence _generateDotBuildfileContent = this.generateDotBuildfileContent();
     fsa.generateFile("../doc-gen/dot/build.xml", _generateDotBuildfileContent);
   }
   
   private CharSequence generateTransition(final Transition transition) {
     if (transition instanceof DecisionTransition) {
       return _generateTransition((DecisionTransition)transition);
     } else if (transition instanceof FinalTransition) {
       return _generateTransition((FinalTransition)transition);
     } else if (transition instanceof InitialTransition) {
       return _generateTransition((InitialTransition)transition);
     } else if (transition instanceof NonInitialTransition) {
       return _generateTransition((NonInitialTransition)transition);
     } else {
       throw new IllegalArgumentException("Unhandled parameter types: " +
         Arrays.<Object>asList(transition).toString());
     }
   }
 }
