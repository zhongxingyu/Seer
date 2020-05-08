 package org.eclipse.gmf.codegen.templates.lite.parts;
 
 import java.util.*;
 import org.eclipse.emf.codegen.ecore.genmodel.*;
 import org.eclipse.gmf.codegen.gmfgen.*;
 import org.eclipse.gmf.codegen.gmfgen.util.*;
 import org.eclipse.gmf.common.codegen.*;
 import org.eclipse.emf.ecore.*;
 
 public class NodeEditPartGenerator
 {
   protected static String nl;
   public static synchronized NodeEditPartGenerator create(String lineSeparator)
   {
     nl = lineSeparator;
     NodeEditPartGenerator result = new NodeEditPartGenerator();
     nl = null;
     return result;
   }
 
   protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
   protected final String TEXT_1 = "";
   protected final String TEXT_2 = NL;
   protected final String TEXT_3 = "((";
   protected final String TEXT_4 = ")";
   protected final String TEXT_5 = "((";
   protected final String TEXT_6 = ")";
   protected final String TEXT_7 = ")";
   protected final String TEXT_8 = ".eGet(";
   protected final String TEXT_9 = ".eINSTANCE.get";
   protected final String TEXT_10 = "())";
   protected final String TEXT_11 = ")";
   protected final String TEXT_12 = "((";
   protected final String TEXT_13 = ")";
   protected final String TEXT_14 = ")";
   protected final String TEXT_15 = ".";
   protected final String TEXT_16 = "()";
   protected final String TEXT_17 = NL;
   protected final String TEXT_18 = NL;
   protected final String TEXT_19 = NL;
   protected final String TEXT_20 = NL + "/*" + NL + " * ";
   protected final String TEXT_21 = NL + " */";
   protected final String TEXT_22 = NL;
   protected final String TEXT_23 = NL + NL + "/**" + NL + " * @generated" + NL + " */" + NL + "public class ";
   protected final String TEXT_24 = " extends ";
   protected final String TEXT_25 = " implements ";
   protected final String TEXT_26 = ", ";
   protected final String TEXT_27 = ", ";
   protected final String TEXT_28 = " {";
   protected final String TEXT_29 = NL;
   protected final String TEXT_30 = NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic static final int VISUAL_ID = ";
   protected final String TEXT_31 = ";";
   protected final String TEXT_32 = NL + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected ";
   protected final String TEXT_33 = " contentPane;" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected ";
   protected final String TEXT_34 = " primaryShape;" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate ";
   protected final String TEXT_35 = " myDecorationManager;" + NL;
   protected final String TEXT_36 = NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected ";
   protected final String TEXT_37 = " childNodesPane;";
   protected final String TEXT_38 = NL + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic ";
   protected final String TEXT_39 = "(";
   protected final String TEXT_40 = " model) {" + NL + "\t\tassert model instanceof ";
   protected final String TEXT_41 = ";" + NL + "\t\tsetModel(model);" + NL + "\t}" + NL;
   protected final String TEXT_42 = NL;
   protected final String TEXT_43 = "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected void registerModel() {" + NL + "\t\tsuper.registerModel();" + NL + "\t\t";
   protected final String TEXT_44 = " view = (";
   protected final String TEXT_45 = ") getModel();" + NL + "\t\tif (view != null && view.isSetElement() && view.getElement() != null) {" + NL + "\t\t\tgetViewer().getEditPartRegistry().put(view.getElement(), this);\t" + NL + "\t\t}" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected void unregisterModel() {" + NL + "\t\tsuper.unregisterModel();" + NL + "\t\t";
   protected final String TEXT_46 = " view = (";
   protected final String TEXT_47 = ") getModel();" + NL + "\t\tif (view != null && view.isSetElement() && view.getElement() != null && getViewer().getEditPartRegistry().get(view.getElement()) == this) {" + NL + "\t\t\tgetViewer().getEditPartRegistry().remove(view.getElement());" + NL + "\t\t}" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected void createEditPolicies() {";
   protected final String TEXT_48 = NL;
   protected final String TEXT_49 = "\t\tinstallEditPolicy(";
   protected final String TEXT_50 = ".COMPONENT_ROLE, new ";
   protected final String TEXT_51 = "() {" + NL + "\t\t\tprotected ";
   protected final String TEXT_52 = " createDeleteCommand(";
   protected final String TEXT_53 = " deleteRequest) {" + NL + "\t\t\t\t";
   protected final String TEXT_54 = " editingDomain = ";
   protected final String TEXT_55 = ".getEditingDomain(getDiagramNode().getDiagram().getElement());" + NL + "\t\t\t\t";
   protected final String TEXT_56 = " cc = new ";
   protected final String TEXT_57 = "();" + NL + "\t\t\t\tcc.append(getDomainModelRemoveCommand(editingDomain));" + NL + "\t\t\t\tcc.append(new ";
   protected final String TEXT_58 = "((";
   protected final String TEXT_59 = ") getDiagramNode().eContainer(), getDiagramNode()));" + NL + "\t\t\t\treturn new ";
   protected final String TEXT_60 = "(editingDomain, cc);" + NL + "\t\t\t}" + NL + "" + NL + "\t\t\tprivate org.eclipse.emf.common.command.Command getDomainModelRemoveCommand(";
   protected final String TEXT_61 = " editingDomain) {";
   protected final String TEXT_62 = NL + "\t\t\t\t";
   protected final String TEXT_63 = " result = new ";
   protected final String TEXT_64 = "();";
   protected final String TEXT_65 = NL + "\t\t\t\tresult.append(";
   protected final String TEXT_66 = ".create(" + NL + "\t\t\t\t\teditingDomain, " + NL + "\t\t\t\t\tgetDiagramNode().getElement().eContainer(), ";
   protected final String TEXT_67 = ".eINSTANCE.get";
   protected final String TEXT_68 = "()," + NL + "\t\t\t\t\tgetDiagramNode().getElement()));";
   protected final String TEXT_69 = NL + "\t\t\t\tresult.append(";
   protected final String TEXT_70 = ".create(" + NL + "\t\t\t\t\teditingDomain, " + NL + "\t\t\t\t\tgetDiagramNode().getElement().eContainer(), ";
   protected final String TEXT_71 = ".eINSTANCE.get";
   protected final String TEXT_72 = "()," + NL + "\t\t\t\t\t";
   protected final String TEXT_73 = ".UNSET_VALUE));";
   protected final String TEXT_74 = NL + "\t\t\t\tresult.append(";
   protected final String TEXT_75 = ".create(" + NL + "\t\t\t\t\teditingDomain, " + NL + "\t\t\t\t\tgetDiagramNode().getElement().eContainer(), ";
   protected final String TEXT_76 = ".eINSTANCE.get";
   protected final String TEXT_77 = "()," + NL + "\t\t\t\t\tgetDiagramNode().getElement()));";
   protected final String TEXT_78 = NL + "\t\t\t\tresult.append(";
   protected final String TEXT_79 = ".create(" + NL + "\t\t\t\t\teditingDomain, " + NL + "\t\t\t\t\tgetDiagramNode().getElement().eContainer(), ";
   protected final String TEXT_80 = ".eINSTANCE.get";
   protected final String TEXT_81 = "()," + NL + "\t\t\t\t\t";
   protected final String TEXT_82 = ".UNSET_VALUE));";
   protected final String TEXT_83 = NL + "\t\t\t\treturn result;";
   protected final String TEXT_84 = NL + "\t\t\t\treturn ";
   protected final String TEXT_85 = ".create(" + NL + "\t\t\t\t\teditingDomain, " + NL + "\t\t\t\t\tgetDiagramNode().getElement().eContainer(), ";
   protected final String TEXT_86 = ".eINSTANCE.get";
   protected final String TEXT_87 = "()," + NL + "\t\t\t\t\tgetDiagramNode().getElement());";
   protected final String TEXT_88 = NL + "\t\t\t\treturn ";
   protected final String TEXT_89 = ".create(" + NL + "\t\t\t\t\teditingDomain, " + NL + "\t\t\t\t\tgetDiagramNode().getElement().eContainer(), ";
   protected final String TEXT_90 = ".eINSTANCE.get";
   protected final String TEXT_91 = "()," + NL + "\t\t\t\t\t";
   protected final String TEXT_92 = ".UNSET_VALUE);";
   protected final String TEXT_93 = NL + "\t\t\t}" + NL + "\t\t});";
   protected final String TEXT_94 = NL + "\t\tinstallEditPolicy(";
   protected final String TEXT_95 = ".LAYOUT_ROLE, new ";
   protected final String TEXT_96 = "() {";
   protected final String TEXT_97 = NL;
   protected final String TEXT_98 = NL + "protected Command createAddCommand(final ";
   protected final String TEXT_99 = " child, final Object constraint) {" + NL + "\tif (child.getModel() instanceof ";
   protected final String TEXT_100 = ") {" + NL + "\t\tfinal ";
   protected final String TEXT_101 = " childNode = (";
   protected final String TEXT_102 = ") child.getModel();" + NL + "\t\tfinal ";
   protected final String TEXT_103 = " editingDomain = ";
   protected final String TEXT_104 = ".getEditingDomain(childNode.getDiagram().getElement());" + NL + "\t\tString modelID = ";
   protected final String TEXT_105 = ".getModelID(childNode);" + NL + "\t\tif (";
   protected final String TEXT_106 = ".MODEL_ID.equals(modelID)) {" + NL + "\t\t\tfinal int visualID = ";
   protected final String TEXT_107 = ".getVisualID(childNode);" + NL + "\t\t\tfinal int newVisualID = ";
   protected final String TEXT_108 = ".INSTANCE.getNodeVisualID(";
   protected final String TEXT_109 = ", childNode.getElement());" + NL + "\t\t\t";
   protected final String TEXT_110 = " command = null;" + NL + "\t\t\tswitch (newVisualID) {";
   protected final String TEXT_111 = NL + "\t\t\tcase ";
   protected final String TEXT_112 = ".VISUAL_ID:" + NL + "\t\t\t\t{" + NL + "\t\t\t\t\t";
   protected final String TEXT_113 = " result = new ";
   protected final String TEXT_114 = "();" + NL + "\t\t\t\t\t";
   protected final String TEXT_115 = " element = childNode.getElement();";
   protected final String TEXT_116 = NL + "\t\t\t\t\tswitch (visualID) {";
   protected final String TEXT_117 = NL + "\t\t\t\t\tcase ";
   protected final String TEXT_118 = ".VISUAL_ID:";
   protected final String TEXT_119 = NL + "\t\t\t\t\tresult.append(";
   protected final String TEXT_120 = ".create(" + NL + "\t\t\t\t\t\teditingDomain, " + NL + "\t\t\t\t\t\telement.eContainer(), ";
   protected final String TEXT_121 = ".eINSTANCE.get";
   protected final String TEXT_122 = "()," + NL + "\t\t\t\t\t\telement));";
   protected final String TEXT_123 = NL + "\t\t\t\t\tresult.append(";
   protected final String TEXT_124 = ".create(" + NL + "\t\t\t\t\t\teditingDomain, " + NL + "\t\t\t\t\t\telement.eContainer(), ";
   protected final String TEXT_125 = ".eINSTANCE.get";
   protected final String TEXT_126 = "()," + NL + "\t\t\t\t\t\t";
   protected final String TEXT_127 = ".UNSET_VALUE));";
   protected final String TEXT_128 = NL + "\t\t\t\t\tresult.append(";
   protected final String TEXT_129 = ".create(" + NL + "\t\t\t\t\t\teditingDomain, " + NL + "\t\t\t\t\t\telement.eContainer(), ";
   protected final String TEXT_130 = ".eINSTANCE.get";
   protected final String TEXT_131 = "()," + NL + "\t\t\t\t\t\telement));";
   protected final String TEXT_132 = NL + "\t\t\t\t\tresult.append(";
   protected final String TEXT_133 = ".create(" + NL + "\t\t\t\t\t\teditingDomain, " + NL + "\t\t\t\t\t\telement.eContainer(), ";
   protected final String TEXT_134 = ".eINSTANCE.get";
   protected final String TEXT_135 = "()," + NL + "\t\t\t\t\t\t";
   protected final String TEXT_136 = ".UNSET_VALUE));";
   protected final String TEXT_137 = NL + "\t\t\t\t\tbreak;";
   protected final String TEXT_138 = NL + "\t\t\t\t\t}\t";
   protected final String TEXT_139 = NL;
   protected final String TEXT_140 = NL + "\t\tif (";
   protected final String TEXT_141 = " != null) {" + NL + "\t\t\t";
   protected final String TEXT_142 = NL + "\t\t}";
   protected final String TEXT_143 = NL + "\t\tif (";
   protected final String TEXT_144 = ".size() >= ";
   protected final String TEXT_145 = ".eINSTANCE.get";
   protected final String TEXT_146 = "().getUpperBound()) {" + NL + "\t\t\t";
   protected final String TEXT_147 = NL + "\t\t}";
   protected final String TEXT_148 = NL + "\t\t\t\t\tresult.append(";
   protected final String TEXT_149 = ".create(" + NL + "\t\t\t\t\t\teditingDomain," + NL + "\t\t\t\t\t\t";
   protected final String TEXT_150 = ".getElement(), ";
   protected final String TEXT_151 = ".eINSTANCE.get";
   protected final String TEXT_152 = "(), element));";
   protected final String TEXT_153 = NL;
   protected final String TEXT_154 = NL + "\t\tif (";
   protected final String TEXT_155 = " != null) {" + NL + "\t\t\t";
   protected final String TEXT_156 = NL + "\t\t}";
   protected final String TEXT_157 = NL + "\t\tif (";
   protected final String TEXT_158 = ".size() >= ";
   protected final String TEXT_159 = ".eINSTANCE.get";
   protected final String TEXT_160 = "().getUpperBound()) {" + NL + "\t\t\t";
   protected final String TEXT_161 = NL + "\t\t}";
   protected final String TEXT_162 = NL + "\t\t\t\t\tresult.append(";
   protected final String TEXT_163 = ".create(" + NL + "\t\t\t\t\t\teditingDomain," + NL + "\t\t\t\t\t\t";
   protected final String TEXT_164 = ".getElement(), ";
   protected final String TEXT_165 = ".eINSTANCE.get";
   protected final String TEXT_166 = "(), element));";
   protected final String TEXT_167 = NL + "\t\t\t\t\tresult.append(";
   protected final String TEXT_168 = ".create(editingDomain, childNode.eContainer(), childNode.eContainmentFeature(), childNode));" + NL + "\t\t\t\t\tif (newVisualID == visualID) {" + NL + "\t\t\t\t\t\tresult.append(new ";
   protected final String TEXT_169 = "(";
   protected final String TEXT_170 = "));" + NL + "\t\t\t\t\t\tresult.append(";
   protected final String TEXT_171 = ".create(editingDomain, ";
   protected final String TEXT_172 = ", ";
   protected final String TEXT_173 = ".eINSTANCE.getView_PersistedChildren(), childNode));" + NL + "\t\t\t\t\t\tresult.append(new ";
   protected final String TEXT_174 = "(childNode, ";
   protected final String TEXT_175 = "null";
   protected final String TEXT_176 = "(";
   protected final String TEXT_177 = ") constraint";
   protected final String TEXT_178 = "));" + NL + "\t\t\t\t\t} else {";
   protected final String TEXT_179 = NL + "\t\t\t\t\t\tresult.append(new ";
   protected final String TEXT_180 = "(";
   protected final String TEXT_181 = ", element, ";
   protected final String TEXT_182 = "null";
   protected final String TEXT_183 = "((";
   protected final String TEXT_184 = ") constraint).getCopy().union(new ";
   protected final String TEXT_185 = "(";
   protected final String TEXT_186 = ", ";
   protected final String TEXT_187 = "))";
   protected final String TEXT_188 = ", ";
   protected final String TEXT_189 = ".INSTANCE));";
   protected final String TEXT_190 = NL + "\t\t\t\t\t}" + NL + "\t\t\t\t\tcommand = result;" + NL + "\t\t\t\t}" + NL + "\t\t\t\tbreak;";
   protected final String TEXT_191 = NL + "\t\t\t}" + NL + "\t\t\tif (command != null) {" + NL + "\t\t\t\treturn new ";
   protected final String TEXT_192 = "(editingDomain, command);" + NL + "\t\t\t}" + NL + "\t\t}" + NL + "\t}" + NL + "\treturn ";
   protected final String TEXT_193 = ".INSTANCE;" + NL + "}";
   protected final String TEXT_194 = NL;
   protected final String TEXT_195 = NL + "protected ";
   protected final String TEXT_196 = " getCloneCommand(";
   protected final String TEXT_197 = " request) {" + NL + "\t";
   protected final String TEXT_198 = " editParts = request.getEditParts();" + NL + "\t";
   protected final String TEXT_199 = " command = new ";
   protected final String TEXT_200 = "();" + NL + "\tcommand.setDebugLabel(\"Clone in ConstrainedLayoutEditPolicy\");//$NON-NLS-1$" + NL + "\t";
   protected final String TEXT_201 = " childPart;" + NL + "\t";
   protected final String TEXT_202 = " r;" + NL + "\tObject constraint;" + NL + "" + NL + "\tfor (int i = 0; i < editParts.size(); i++) {" + NL + "\t\tchildPart = (";
   protected final String TEXT_203 = ")editParts.get(i);" + NL + "\t\tr = childPart.getFigure().getBounds().getCopy();" + NL + "\t\t//convert r to absolute from childpart figure" + NL + "\t\tchildPart.getFigure().translateToAbsolute(r);" + NL + "\t\tr = request.getTransformedRectangle(r);" + NL + "\t\t//convert this figure to relative " + NL + "\t\tgetLayoutContainer().translateToRelative(r);" + NL + "\t\tgetLayoutContainer().translateFromParent(r);" + NL + "\t\tr.translate(getLayoutOrigin().getNegated());" + NL + "\t\tconstraint = getConstraintFor(r);" + NL + "\t\tcommand.add(createCloneCommand(childPart," + NL + "\t\t\ttranslateToModelConstraint(constraint)));" + NL + "\t}" + NL + "\treturn command.unwrap();" + NL + "}" + NL + "protected ";
   protected final String TEXT_204 = " createCloneCommand(final ";
   protected final String TEXT_205 = " child, final Object constraint) {" + NL + "\tif (child.getModel() instanceof ";
   protected final String TEXT_206 = ") {" + NL + "\t\tfinal ";
   protected final String TEXT_207 = " childNode = (";
   protected final String TEXT_208 = ") child.getModel();" + NL + "\t\tfinal ";
   protected final String TEXT_209 = " editingDomain = ";
   protected final String TEXT_210 = ".getEditingDomain(childNode.getDiagram().getElement());" + NL + "\t\tString modelID = ";
   protected final String TEXT_211 = ".getModelID(childNode);" + NL + "\t\tif (";
   protected final String TEXT_212 = ".MODEL_ID.equals(modelID)) {" + NL + "\t\t\tfinal int newVisualID = ";
   protected final String TEXT_213 = ".INSTANCE.getNodeVisualID(";
   protected final String TEXT_214 = ", childNode.getElement());" + NL + "\t\t\t";
   protected final String TEXT_215 = " command = null;" + NL + "\t\t\tswitch (newVisualID) {";
   protected final String TEXT_216 = NL + "\t\t\tcase ";
   protected final String TEXT_217 = ".VISUAL_ID:" + NL + "\t\t\t\tcommand = new ";
   protected final String TEXT_218 = "() {" + NL + "\t\t\t\t\tprivate ";
   protected final String TEXT_219 = " afterCopyCommand;" + NL + "\t\t\t\t\tprotected ";
   protected final String TEXT_220 = " createCommand() {" + NL + "\t\t\t\t\t\t";
   protected final String TEXT_221 = " element = childNode.getElement();" + NL + "\t\t\t\t\t\t//We are being optimistic here about whether further commands can be executed." + NL + "\t\t\t\t\t\t//Otherwise, we would have to execute the CopyCommand on every mouse move, which could be much too expensive.  " + NL + "\t\t\t\t\t\treturn ";
   protected final String TEXT_222 = ".create(editingDomain, element);" + NL + "\t\t\t\t\t}" + NL + "\t\t\t\t\tprotected boolean prepare() {" + NL + "\t\t\t\t\t\tif (!super.prepare()) {" + NL + "\t\t\t\t\t\t\treturn false;" + NL + "\t\t\t\t\t\t}";
   protected final String TEXT_223 = NL;
   protected final String TEXT_224 = NL + "\t\tif (";
   protected final String TEXT_225 = " != null) {" + NL + "\t\t\t";
   protected final String TEXT_226 = NL + "\t\t}";
   protected final String TEXT_227 = NL + "\t\tif (";
   protected final String TEXT_228 = ".size() >= ";
   protected final String TEXT_229 = ".eINSTANCE.get";
   protected final String TEXT_230 = "().getUpperBound()) {" + NL + "\t\t\t";
   protected final String TEXT_231 = NL + "\t\t}";
   protected final String TEXT_232 = NL;
   protected final String TEXT_233 = NL + "\t\tif (";
   protected final String TEXT_234 = " != null) {" + NL + "\t\t\t";
   protected final String TEXT_235 = NL + "\t\t}";
   protected final String TEXT_236 = NL + "\t\tif (";
   protected final String TEXT_237 = ".size() >= ";
   protected final String TEXT_238 = ".eINSTANCE.get";
   protected final String TEXT_239 = "().getUpperBound()) {" + NL + "\t\t\t";
   protected final String TEXT_240 = NL + "\t\t}";
   protected final String TEXT_241 = NL + "\t\t\t\t\t\treturn true;" + NL + "\t\t\t\t\t}" + NL + "\t\t\t\t\tpublic void execute() {" + NL + "\t\t\t\t\t\tsuper.execute();" + NL + "\t\t\t\t\t\tfinal ";
   protected final String TEXT_242 = " results = super.getResult();" + NL + "\t\t\t\t\t\tassert results.size() == 1;" + NL + "\t\t\t\t\t\t";
   protected final String TEXT_243 = " result = (";
   protected final String TEXT_244 = ") results.iterator().next();" + NL + "\t\t\t\t\t\tafterCopyCommand = new ";
   protected final String TEXT_245 = "();";
   protected final String TEXT_246 = NL + "\t\t\t\t\t\tafterCopyCommand.append(";
   protected final String TEXT_247 = ".create(" + NL + "\t\t\t\t\t\t\teditingDomain," + NL + "\t\t\t\t\t\t\t";
   protected final String TEXT_248 = ".getElement(), ";
   protected final String TEXT_249 = ".eINSTANCE.get";
   protected final String TEXT_250 = "(), result));";
   protected final String TEXT_251 = NL + "\t\t\t\t\t\tafterCopyCommand.append(";
   protected final String TEXT_252 = ".create(" + NL + "\t\t\t\t\t\t\teditingDomain," + NL + "\t\t\t\t\t\t\t";
   protected final String TEXT_253 = ".getElement(), ";
   protected final String TEXT_254 = ".eINSTANCE.get";
   protected final String TEXT_255 = "(), result));";
   protected final String TEXT_256 = NL + "\t\t\t\t\t\tafterCopyCommand.append(new ";
   protected final String TEXT_257 = "(";
   protected final String TEXT_258 = ", result, ";
   protected final String TEXT_259 = "null";
   protected final String TEXT_260 = "((";
   protected final String TEXT_261 = ") constraint).getCopy().union(new ";
   protected final String TEXT_262 = "(";
   protected final String TEXT_263 = ", ";
   protected final String TEXT_264 = "))";
   protected final String TEXT_265 = ", ";
   protected final String TEXT_266 = ".INSTANCE));" + NL + "\t\t\t\t\t\tif (afterCopyCommand.canExecute()) {" + NL + "\t\t\t\t\t\t\tafterCopyCommand.execute();" + NL + "\t\t\t\t\t\t} else {" + NL + "\t\t\t\t\t\t\tassert false;" + NL + "\t\t\t\t\t\t}" + NL + "\t\t\t\t\t}" + NL + "\t\t\t\t\tpublic void undo() {" + NL + "\t\t\t\t\t\tafterCopyCommand.undo();" + NL + "\t\t\t\t\t\tsuper.undo();" + NL + "\t\t\t\t\t}" + NL + "\t\t\t\t\tpublic void redo() {" + NL + "\t\t\t\t\t\tsuper.redo();" + NL + "\t\t\t\t\t\tafterCopyCommand.redo();" + NL + "\t\t\t\t\t}" + NL + "\t\t\t\t};" + NL + "\t\t\t\tbreak;";
   protected final String TEXT_267 = NL + "\t\t\t}" + NL + "\t\t\tif (command != null) {" + NL + "\t\t\t\treturn new ";
   protected final String TEXT_268 = "(editingDomain, command);" + NL + "\t\t\t}" + NL + "\t\t}" + NL + "\t}" + NL + "\treturn ";
   protected final String TEXT_269 = ".INSTANCE;" + NL + "}";
   protected final String TEXT_270 = NL + "\t\t\tprotected ";
   protected final String TEXT_271 = " getCreateCommand(";
   protected final String TEXT_272 = " request) {";
   protected final String TEXT_273 = NL + "\t\t\t\tif (request instanceof ";
   protected final String TEXT_274 = ") {" + NL + "\t\t\t\t\t";
   protected final String TEXT_275 = " requestEx = (";
   protected final String TEXT_276 = ") request;" + NL + "\t\t\t\t\tint[] visualIds = requestEx.getVisualIds();" + NL + "\t\t\t\t\t";
   protected final String TEXT_277 = " result = new ";
   protected final String TEXT_278 = "();" + NL + "\t\t\t\t\tfor(int i = 0; i < visualIds.length; i++) {" + NL + "\t\t\t\t\t\tint nextVisualId = visualIds[i];" + NL + "\t\t\t\t\t\tswitch (nextVisualId) {";
   protected final String TEXT_279 = NL + "\t\t\t\t\t\tcase ";
   protected final String TEXT_280 = ".VISUAL_ID:" + NL + "\t\t\t\t\t\t\tresult.append(new ";
   protected final String TEXT_281 = "((View) getModel(), requestEx";
   protected final String TEXT_282 = ", " + NL + "\t\t\t\t\t\t\t\t(";
   protected final String TEXT_283 = ")getConstraintFor(request)";
   protected final String TEXT_284 = "));" + NL + "\t\t\t\t\t\t\tbreak;";
   protected final String TEXT_285 = NL + "\t\t\t\t\t\t}" + NL + "\t\t\t\t\t}" + NL + "\t\t\t\t\treturn new ";
   protected final String TEXT_286 = "(";
   protected final String TEXT_287 = ".getEditingDomain(((View) getModel()).getDiagram().getElement()), result);" + NL + "\t\t\t\t}";
   protected final String TEXT_288 = NL + "\t\t\t\treturn ";
   protected final String TEXT_289 = ".INSTANCE;" + NL + "\t\t\t}";
   protected final String TEXT_290 = NL + "\t\t\tprotected ";
   protected final String TEXT_291 = " createChangeConstraintCommand(final ";
   protected final String TEXT_292 = " request, final ";
   protected final String TEXT_293 = " child, Object constraint) {" + NL + "\t\t\t\tif (!isDirectChild(child)) {" + NL + "\t\t\t\t\treturn ";
   protected final String TEXT_294 = ".INSTANCE;" + NL + "\t\t\t\t}";
   protected final String TEXT_295 = NL;
   protected final String TEXT_296 = "\t\t\t\tfinal ";
   protected final String TEXT_297 = " node = (";
   protected final String TEXT_298 = ") child.getModel();" + NL + "\t\t\t\t";
   protected final String TEXT_299 = " emfCommand = new ";
   protected final String TEXT_300 = "(node, request, ((";
   protected final String TEXT_301 = ")child).getFigure());" + NL + "\t\t\t\treturn new ";
   protected final String TEXT_302 = "(";
   protected final String TEXT_303 = ".getEditingDomain(node.getDiagram().getElement()), emfCommand);" + NL + "\t\t\t}" + NL + "\t\t\tprotected Command createChangeConstraintCommand(";
   protected final String TEXT_304 = " child, Object constraint) {" + NL + "\t\t\t\tassert false;" + NL + "\t\t\t\treturn ";
   protected final String TEXT_305 = ".INSTANCE;" + NL + "\t\t\t}";
   protected final String TEXT_306 = NL + "\t\t\tprotected Object getConstraintFor(";
   protected final String TEXT_307 = " rect) {" + NL + "\t\t\t\treturn null;" + NL + "\t\t\t}" + NL + "\t\t\tprotected Object getConstraintFor(";
   protected final String TEXT_308 = " point) {" + NL + "\t\t\t\treturn null;" + NL + "\t\t\t}" + NL + "\t\t\tprotected ";
   protected final String TEXT_309 = " createChangeConstraintCommand(";
   protected final String TEXT_310 = " child, Object constraint) {" + NL + "\t\t\t\treturn ";
   protected final String TEXT_311 = ".INSTANCE;" + NL + "\t\t\t}";
   protected final String TEXT_312 = NL + "\t\t\tprotected ";
   protected final String TEXT_313 = " createChildEditPolicy(";
   protected final String TEXT_314 = " child) {";
   protected final String TEXT_315 = NL + "\t\t\t\tif (isDirectChild(child)) {" + NL + "\t\t\t\t\t";
   protected final String TEXT_316 = " result = child.getEditPolicy(";
   protected final String TEXT_317 = ".PRIMARY_DRAG_ROLE);" + NL + "\t\t\t\t\tif (result != null) {" + NL + "\t\t\t\t\t\treturn result;" + NL + "\t\t\t\t\t}" + NL + "\t\t\t\t\treturn super.createChildEditPolicy(child);" + NL + "\t\t\t\t}";
   protected final String TEXT_318 = NL + "\t\t\t\treturn new ";
   protected final String TEXT_319 = "() {" + NL + "\t\t\t\t\tpublic ";
   protected final String TEXT_320 = " getTargetEditPart(";
   protected final String TEXT_321 = " request) {" + NL + "\t\t\t\t\t\tif (";
   protected final String TEXT_322 = ".REQ_SELECTION.equals(request.getType())) {" + NL + "\t\t\t\t\t\t\treturn ";
   protected final String TEXT_323 = ".this;" + NL + "\t\t\t\t\t\t}" + NL + "\t\t\t\t\t\treturn super.getTargetEditPart(request);" + NL + "\t\t\t\t\t}" + NL + "\t\t\t\t};" + NL + "\t\t\t}" + NL + "\t\t});" + NL + "\t\tinstallEditPolicy(";
   protected final String TEXT_324 = ".GRAPHICAL_NODE_ROLE, new ";
   protected final String TEXT_325 = "() {" + NL + "\t\t\tprotected ";
   protected final String TEXT_326 = " getReconnectTargetCommand(";
   protected final String TEXT_327 = " request) {";
   protected final String TEXT_328 = " " + NL + "\t\t\t\treturn ";
   protected final String TEXT_329 = ".INSTANCE;";
   protected final String TEXT_330 = NL + "\t\t\t\t";
   protected final String TEXT_331 = " connection = request.getConnectionEditPart();" + NL + "\t\t\t\tif (connection.getModel() instanceof ";
   protected final String TEXT_332 = " == false) {" + NL + "\t\t\t\t\treturn ";
   protected final String TEXT_333 = ".INSTANCE;" + NL + "\t\t\t\t}" + NL + "\t\t\t\t";
   protected final String TEXT_334 = " edge = (";
   protected final String TEXT_335 = ")connection.getModel();" + NL + "\t\t\t\tString modelID = ";
   protected final String TEXT_336 = ".getModelID(edge);" + NL + "\t\t\t\tif (!";
   protected final String TEXT_337 = ".MODEL_ID.equals(modelID)) {" + NL + "\t\t\t\t\treturn ";
   protected final String TEXT_338 = ".INSTANCE;" + NL + "\t\t\t\t}" + NL + "\t\t\t\t";
   protected final String TEXT_339 = " result = null;" + NL + "\t\t\t\tint visualID = ";
   protected final String TEXT_340 = ".getVisualID(edge);" + NL + "\t\t\t\tswitch (visualID) {";
   protected final String TEXT_341 = NL + "\t\t\t\tcase ";
   protected final String TEXT_342 = ".VISUAL_ID:" + NL + "\t\t\t\t\tresult = new ";
   protected final String TEXT_343 = "(request);" + NL + "\t\t\t\t\tbreak;";
   protected final String TEXT_344 = NL + "\t\t\t\t}" + NL + "\t\t\t\tif (result == null) {" + NL + "\t\t\t\t\treturn ";
   protected final String TEXT_345 = ".INSTANCE;" + NL + "\t\t\t\t}" + NL + "\t\t\t\treturn new ";
   protected final String TEXT_346 = "(";
   protected final String TEXT_347 = ".getEditingDomain(getDiagramNode().getDiagram().getElement()), result);";
   protected final String TEXT_348 = NL + "\t\t\t}" + NL + "\t\t\tprotected ";
   protected final String TEXT_349 = " getReconnectSourceCommand(";
   protected final String TEXT_350 = " request) {";
   protected final String TEXT_351 = " " + NL + "\t\t\t\treturn ";
   protected final String TEXT_352 = ".INSTANCE;";
   protected final String TEXT_353 = NL + "\t\t\t\t";
   protected final String TEXT_354 = " connection = request.getConnectionEditPart();" + NL + "\t\t\t\tif (connection.getModel() instanceof ";
   protected final String TEXT_355 = " == false) {" + NL + "\t\t\t\t\treturn ";
   protected final String TEXT_356 = ".INSTANCE;" + NL + "\t\t\t\t}" + NL + "\t\t\t\t";
   protected final String TEXT_357 = " edge = (";
   protected final String TEXT_358 = ")connection.getModel();" + NL + "\t\t\t\tString modelID = ";
   protected final String TEXT_359 = ".getModelID(edge);" + NL + "\t\t\t\tif (!";
   protected final String TEXT_360 = ".MODEL_ID.equals(modelID)) {" + NL + "\t\t\t\t\treturn ";
   protected final String TEXT_361 = ".INSTANCE;" + NL + "\t\t\t\t}" + NL + "\t\t\t\t";
   protected final String TEXT_362 = " result = null;" + NL + "\t\t\t\tint visualID = ";
   protected final String TEXT_363 = ".getVisualID(edge);" + NL + "\t\t\t\tswitch (visualID) {";
   protected final String TEXT_364 = NL + "\t\t\t\tcase ";
   protected final String TEXT_365 = ".VISUAL_ID:" + NL + "\t\t\t\t\tresult = new ";
   protected final String TEXT_366 = "(request);" + NL + "\t\t\t\t\tbreak;";
   protected final String TEXT_367 = NL + "\t\t\t\t}" + NL + "\t\t\t\tif (result == null) {" + NL + "\t\t\t\t\treturn ";
   protected final String TEXT_368 = ".INSTANCE;" + NL + "\t\t\t\t}" + NL + "\t\t\t\treturn new ";
   protected final String TEXT_369 = "(";
   protected final String TEXT_370 = ".getEditingDomain(getDiagramNode().getDiagram().getElement()), result);";
   protected final String TEXT_371 = NL + "\t\t\t}" + NL + "\t\t\tprotected ";
   protected final String TEXT_372 = " getConnectionCreateCommand(";
   protected final String TEXT_373 = " request) {";
   protected final String TEXT_374 = NL + "\t\t\t\tif (request instanceof ";
   protected final String TEXT_375 = ") {" + NL + "\t\t\t\t\t";
   protected final String TEXT_376 = " requestEx = (";
   protected final String TEXT_377 = ") request;" + NL + "\t\t\t\t\tint[] visualIds = requestEx.getVisualIds();" + NL + "\t\t\t\t\t";
   protected final String TEXT_378 = " result = new ";
   protected final String TEXT_379 = "();" + NL + "\t\t\t\t\tfor (int i = 0; i < visualIds.length; i++) {" + NL + "\t\t\t\t\t\tint nextVisualId = visualIds[i];" + NL + "\t\t\t\t\t\tswitch (nextVisualId) {";
   protected final String TEXT_380 = NL + "\t\t\t\t\t\tcase ";
   protected final String TEXT_381 = ".VISUAL_ID:" + NL + "\t\t\t\t\t\t\tresult.appendIfCanExecute(new ";
   protected final String TEXT_382 = "(requestEx));" + NL + "\t\t\t\t\t\t\tbreak;";
   protected final String TEXT_383 = NL + "\t\t\t\t\t\t}" + NL + "\t\t\t\t\t}" + NL + "\t\t\t\t\tif (!result.canExecute()) {" + NL + "\t\t\t\t\t\t//returning an unexecutable command does not change cursor to \"No\"." + NL + "\t\t\t\t\t\treturn null;" + NL + "\t\t\t\t\t}" + NL + "\t\t\t\t\t";
   protected final String TEXT_384 = " wrappedResult = new ";
   protected final String TEXT_385 = "(";
   protected final String TEXT_386 = ".getEditingDomain(getDiagramNode().getDiagram().getElement()), result);" + NL + "\t\t\t\t\trequest.setStartCommand(wrappedResult);" + NL + "\t\t\t\t\treturn wrappedResult;" + NL + "\t\t\t\t}";
   protected final String TEXT_387 = NL + "\t\t\t\treturn null;" + NL + "\t\t\t}" + NL + "\t\t\tprotected ";
   protected final String TEXT_388 = " getConnectionCompleteCommand(";
   protected final String TEXT_389 = " request) {";
   protected final String TEXT_390 = NL + "\t\t\t\tif (request.getStartCommand() == null || !request.getStartCommand().canExecute()) {" + NL + "\t\t\t\t\treturn ";
   protected final String TEXT_391 = ".INSTANCE;" + NL + "\t\t\t\t}" + NL + "\t\t\t\tif (request instanceof ";
   protected final String TEXT_392 = ") {" + NL + "\t\t\t\t\t";
   protected final String TEXT_393 = " requestEx = (";
   protected final String TEXT_394 = ") request;" + NL + "\t\t\t\t\tint[] visualIds = requestEx.getVisualIds();" + NL + "\t\t\t\t\t";
   protected final String TEXT_395 = " result = new ";
   protected final String TEXT_396 = "();" + NL + "\t\t\t\t\tfor (int i = 0; i < visualIds.length; i++) {" + NL + "\t\t\t\t\t\tint nextVisualId = visualIds[i];" + NL + "\t\t\t\t\t\tswitch (nextVisualId) {";
   protected final String TEXT_397 = NL + "\t\t\t\t\t\tcase ";
   protected final String TEXT_398 = ".VISUAL_ID:" + NL + "\t\t\t\t\t\t\tresult.appendIfCanExecute(new ";
   protected final String TEXT_399 = "(requestEx));" + NL + "\t\t\t\t\t\t\tbreak;";
   protected final String TEXT_400 = NL + "\t\t\t\t\t\t}" + NL + "\t\t\t\t\t}" + NL + "\t\t\t\t\tif (result.getCommandList().size() != 1 || !result.canExecute()) {" + NL + "\t\t\t\t\t\t//Cannot create several connections at once." + NL + "\t\t\t\t\t\treturn ";
   protected final String TEXT_401 = ".INSTANCE;" + NL + "\t\t\t\t\t}" + NL + "\t\t\t\t\treturn new ";
   protected final String TEXT_402 = "(";
   protected final String TEXT_403 = ".getEditingDomain(getDiagramNode().getDiagram().getElement()), result);" + NL + "\t\t\t\t}";
   protected final String TEXT_404 = NL + "\t\t\t\treturn ";
   protected final String TEXT_405 = ".INSTANCE;" + NL + "\t\t\t}" + NL + "\t\t});";
   protected final String TEXT_406 = NL + "\t\tinstallEditPolicy(";
   protected final String TEXT_407 = ".DIRECT_EDIT_ROLE, new ";
   protected final String TEXT_408 = "());";
   protected final String TEXT_409 = NL;
   protected final String TEXT_410 = NL + "\t\tinstallEditPolicy(\"";
   protected final String TEXT_411 = "\", new ";
   protected final String TEXT_412 = "()); //$NON-NLS-1$";
   protected final String TEXT_413 = NL;
   protected final String TEXT_414 = NL + "\t\tinstallEditPolicy(";
   protected final String TEXT_415 = ".OPEN_ROLE";
   protected final String TEXT_416 = " + \"";
   protected final String TEXT_417 = "\" ";
   protected final String TEXT_418 = ", new ";
   protected final String TEXT_419 = "());";
   protected final String TEXT_420 = NL + "\t\tinstallEditPolicy(org.eclipse.gef.EditPolicy.PRIMARY_DRAG_ROLE, getPrimaryDragEditPolicy());";
   protected final String TEXT_421 = NL + "\t\tinstallNotationModelRefresher();";
   protected final String TEXT_422 = NL + "\t\tinstallLinkNotationModelRefresher();";
   protected final String TEXT_423 = NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected ";
   protected final String TEXT_424 = " getPrimaryDragEditPolicy() {";
   protected final String TEXT_425 = NL + "\t\treturn new ";
   protected final String TEXT_426 = "();";
   protected final String TEXT_427 = NL + "\t\t";
   protected final String TEXT_428 = " result = new ";
   protected final String TEXT_429 = "();";
   protected final String TEXT_430 = NL + "\t\t";
   protected final String TEXT_431 = " result = new ";
   protected final String TEXT_432 = "() {" + NL + "\t\t\tprotected ";
   protected final String TEXT_433 = " createSelectionHandles() {" + NL + "\t\t\t\t";
   protected final String TEXT_434 = " result = super.createSelectionHandles();" + NL + "\t\t\t\tfor(";
   protected final String TEXT_435 = " it = getChildren().iterator(); it.hasNext(); ) {" + NL + "\t\t\t\t\t";
   protected final String TEXT_436 = " next = (";
   protected final String TEXT_437 = ") it.next();" + NL + "\t\t\t\t\t";
   protected final String TEXT_438 = " nextView = (";
   protected final String TEXT_439 = ") next.getModel();" + NL + "\t\t\t\t\tswitch (";
   protected final String TEXT_440 = ".getVisualID(nextView)) {";
   protected final String TEXT_441 = NL + "\t\t\t\t\tcase ";
   protected final String TEXT_442 = ".VISUAL_ID:" + NL + "\t\t\t\t\t\tresult.addAll(((";
   protected final String TEXT_443 = ") next).createSelectionHandles());" + NL + "\t\t\t\t\t\tbreak;";
   protected final String TEXT_444 = NL + "\t\t\t\t\t}" + NL + "\t\t\t\t}" + NL + "\t\t\t\treturn result;" + NL + "\t\t\t}" + NL + "\t\t};";
   protected final String TEXT_445 = NL + "\t\tresult.setResizeDirections(";
   protected final String TEXT_446 = ".NONE);";
   protected final String TEXT_447 = NL + "\t\tresult.setResizeDirections(";
   protected final String TEXT_448 = ".";
   protected final String TEXT_449 = " | ";
   protected final String TEXT_450 = ");";
   protected final String TEXT_451 = NL + "\t\treturn result;";
   protected final String TEXT_452 = NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected ";
   protected final String TEXT_453 = " createFigure() {" + NL + "\t\t";
   protected final String TEXT_454 = " invisibleRectangle = new ";
   protected final String TEXT_455 = "();" + NL + "\t\tinvisibleRectangle.setLayoutManager(new ";
   protected final String TEXT_456 = "());" + NL + "\t\t";
   protected final String TEXT_457 = " shape = createNodeShape();" + NL + "\t\tinvisibleRectangle.add(shape);" + NL + "\t\tcontentPane = setupContentPane(shape);";
   protected final String TEXT_458 = NL + "\t\tchildNodesPane = createChildNodesPane();" + NL + "\t\tshape.add(childNodesPane);";
   protected final String TEXT_459 = NL + NL + "\t\t";
  protected final String TEXT_460 = " decorationShape = createDecorationPane();" + NL + "\t\tif (decorationShape != null) {" + NL + "\t\t\tmyDecorationManager = createDecorationManager(decorationShape);" + NL + "\t\t\tinvisibleRectangle.add(decorationShape);" + NL + "\t\t}" + NL + "" + NL + "\t\treturn invisibleRectangle;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected ";
   protected final String TEXT_461 = " createDecorationManager(";
   protected final String TEXT_462 = " decorationShape) {" + NL + "\t\treturn new ";
   protected final String TEXT_463 = "(decorationShape);" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic ";
   protected final String TEXT_464 = " getDecorationManager() {" + NL + "\t\treturn myDecorationManager;" + NL + "\t}" + NL;
   protected final String TEXT_465 = NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected ";
   protected final String TEXT_466 = " createChildNodesPane() {" + NL + "\t\t";
   protected final String TEXT_467 = " result = new ";
   protected final String TEXT_468 = "();" + NL + "\t\tsetupContentPane(result);" + NL + "\t\treturn result;" + NL + "\t}";
   protected final String TEXT_469 = NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected ";
   protected final String TEXT_470 = " createNodeShape() {";
   protected final String TEXT_471 = NL + "\t\tprimaryShape = new ";
   protected final String TEXT_472 = "()";
   protected final String TEXT_473 = " {" + NL + "\t\t\tprotected boolean useLocalCoordinates() {" + NL + "\t\t\t\treturn true;" + NL + "\t\t\t}" + NL + "\t\t}";
   protected final String TEXT_474 = ";";
   protected final String TEXT_475 = NL + "\t\tprimaryShape = ";
   protected final String TEXT_476 = ";";
   protected final String TEXT_477 = NL + "\t\t";
   protected final String TEXT_478 = " figure = new ";
   protected final String TEXT_479 = "();";
   protected final String TEXT_480 = NL + " \t\tfigure.setUseLocalCoordinates(true);";
   protected final String TEXT_481 = NL + " \t\tprimaryShape = figure;";
   protected final String TEXT_482 = NL + "\t\treturn primaryShape;" + NL + "\t}";
   protected final String TEXT_483 = NL + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic ";
   protected final String TEXT_484 = " getPrimaryShape() {" + NL + "\t\treturn (";
   protected final String TEXT_485 = ") primaryShape;" + NL + "\t}";
   protected final String TEXT_486 = NL + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate ";
   protected final String TEXT_487 = " createDecorationPane() {" + NL + "\t\treturn new ";
   protected final String TEXT_488 = "();" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * Default implementation treats passed figure as content pane." + NL + "\t * Respects layout one may have set for generated figure." + NL + "\t * @param nodeShape instance of generated figure class" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected ";
   protected final String TEXT_489 = " setupContentPane(";
   protected final String TEXT_490 = " nodeShape) {" + NL + "\t\tif (nodeShape.getLayoutManager() == null) {";
   protected final String TEXT_491 = NL + "\t\t\t";
   protected final String TEXT_492 = " layout = new ";
   protected final String TEXT_493 = "();" + NL + "\t\t\tlayout.setSpacing(5);" + NL + "\t\t\tnodeShape.setLayoutManager(layout);";
   protected final String TEXT_494 = NL + "\t\tnodeShape.setLayoutManager(new ";
   protected final String TEXT_495 = "() {" + NL + "" + NL + "\t\t\tpublic Object getConstraint(";
   protected final String TEXT_496 = " figure) {" + NL + "\t\t\t\tObject result = constraints.get(figure);" + NL + "\t\t\t\tif (result == null) {" + NL + "\t\t\t\t\tresult = new ";
   protected final String TEXT_497 = "(0, 0, -1, -1);" + NL + "\t\t\t\t}" + NL + "\t\t\t\treturn result;" + NL + "\t\t\t}" + NL + "\t\t});";
   protected final String TEXT_498 = NL + "\t\t}" + NL + "\t\treturn nodeShape; // use nodeShape itself as contentPane" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic ";
   protected final String TEXT_499 = " getContentPane() {" + NL + "\t\tif (contentPane == null) {" + NL + "\t\t\treturn super.getContentPane();" + NL + "\t\t}" + NL + "\t\treturn contentPane;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic ";
   protected final String TEXT_500 = " getDiagramNode() {" + NL + "\t\treturn (";
   protected final String TEXT_501 = ") getModel();" + NL + "\t}" + NL;
   protected final String TEXT_502 = NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected boolean addFixedChild(";
   protected final String TEXT_503 = " childEditPart) {";
   protected final String TEXT_504 = NL + "\t\tif (childEditPart instanceof ";
   protected final String TEXT_505 = ") {" + NL + "\t\t\t((";
   protected final String TEXT_506 = ") childEditPart).";
   protected final String TEXT_507 = "(getPrimaryShape().";
   protected final String TEXT_508 = "());" + NL + "\t\t\treturn true;" + NL + "\t\t}";
   protected final String TEXT_509 = NL + "\t\tif (childEditPart instanceof ";
   protected final String TEXT_510 = ") {" + NL + "\t\t\t";
   protected final String TEXT_511 = " pane = getPrimaryShape().";
   protected final String TEXT_512 = "();" + NL + "\t\t\tsetupContentPane(pane); // FIXME each comparment should handle his content pane in his own way " + NL + "\t\t\tpane.add(((";
   protected final String TEXT_513 = ")childEditPart).getFigure());" + NL + "\t\t\treturn true;" + NL + "\t\t}\t";
   protected final String TEXT_514 = NL + "\t\treturn false;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected boolean removeFixedChild(EditPart childEditPart) {";
   protected final String TEXT_515 = "\t\t" + NL + "\t\tif (childEditPart instanceof ";
   protected final String TEXT_516 = ") {" + NL + "\t\t\t";
   protected final String TEXT_517 = " pane = getPrimaryShape().";
   protected final String TEXT_518 = "();" + NL + "\t\t\tpane.remove(((";
   protected final String TEXT_519 = ")childEditPart).getFigure());" + NL + "\t\t\treturn true;" + NL + "\t\t}\t";
   protected final String TEXT_520 = NL + "\t\treturn false;" + NL + "\t}";
   protected final String TEXT_521 = NL + NL + "\t/**" + NL + "\t * Returns the label which should be direct-edited by default." + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate ";
   protected final String TEXT_522 = " getPrimaryLabelEditPart() {";
   protected final String TEXT_523 = NL + "\t\tfor(";
   protected final String TEXT_524 = " it = getDiagramNode().getChildren().iterator(); it.hasNext(); ) {" + NL + "\t\t\t";
   protected final String TEXT_525 = " nextChild = (";
   protected final String TEXT_526 = ")it.next();" + NL + "\t\t\tif (";
   protected final String TEXT_527 = ".getVisualID(nextChild) == ";
   protected final String TEXT_528 = ".VISUAL_ID) {" + NL + "\t\t\t\treturn (";
   protected final String TEXT_529 = ") getViewer().getEditPartRegistry().get(nextChild);" + NL + "\t\t\t}" + NL + "\t\t}";
   protected final String TEXT_530 = NL + "\t\treturn null;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic void performRequest(";
   protected final String TEXT_531 = " req) {" + NL + "\t\tif (";
   protected final String TEXT_532 = ".REQ_DIRECT_EDIT.equals(req.getType())) {" + NL + "\t\t\t";
   protected final String TEXT_533 = " labelToEdit;" + NL + "\t\t\tif (req instanceof ";
   protected final String TEXT_534 = ") {" + NL + "\t\t\t\tlabelToEdit = getLabelEditPart((";
   protected final String TEXT_535 = ")req);" + NL + "\t\t\t} else {" + NL + "\t\t\t\tlabelToEdit = getPrimaryLabelEditPart();" + NL + "\t\t\t}" + NL + "\t\t\tif (labelToEdit != null) {" + NL + "\t\t\t\tlabelToEdit.performRequest(req);" + NL + "\t\t\t}" + NL + "\t\t}";
   protected final String TEXT_536 = NL;
   protected final String TEXT_537 = "if (";
   protected final String TEXT_538 = ".REQ_OPEN.equals(req.getType())) {" + NL + "\t";
   protected final String TEXT_539 = " command = getCommand(req);" + NL + "\tif (command != null && command.canExecute()) {" + NL + "\t\tgetViewer().getEditDomain().getCommandStack().execute(command);" + NL + "\t}" + NL + "\treturn;" + NL + "}" + NL + "\t\tsuper.performRequest(req);" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate ";
   protected final String TEXT_540 = " getLabelEditPart(";
   protected final String TEXT_541 = " req) {" + NL + "\t\t";
   protected final String TEXT_542 = " result = getViewer().findObjectAt(req.getLocation());" + NL + "\t\tif (result != null) {" + NL + "\t\t\t";
   protected final String TEXT_543 = " view = (";
   protected final String TEXT_544 = ") result.getModel();" + NL + "\t\t\tif (getDiagramNode().getChildren().contains(view)) {" + NL + "\t\t\t\tint visualId = ";
   protected final String TEXT_545 = ".getVisualID(view);" + NL + "\t\t\t\tswitch (visualId) {";
   protected final String TEXT_546 = NL + "\t\t\t\tcase ";
   protected final String TEXT_547 = ".VISUAL_ID:" + NL + "\t\t\t\t\treturn result;";
   protected final String TEXT_548 = NL + "\t\t\t\t}" + NL + "\t\t\t}" + NL + "\t\t}" + NL + "\t\treturn getPrimaryLabelEditPart();" + NL + "\t}" + NL;
   protected final String TEXT_549 = NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected boolean isExternalLabel(";
   protected final String TEXT_550 = " childEditPart) {";
   protected final String TEXT_551 = NL + "\t\tif (childEditPart instanceof ";
   protected final String TEXT_552 = ") {" + NL + "\t\t\treturn true;" + NL + "\t\t}";
   protected final String TEXT_553 = NL + "\t\treturn false;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected ";
   protected final String TEXT_554 = " getExternalLabelsContainer() {" + NL + "\t\t";
   protected final String TEXT_555 = " root = (";
   protected final String TEXT_556 = ") getRoot();" + NL + "\t\treturn root.getLayer(";
   protected final String TEXT_557 = ".EXTERNAL_NODE_LABELS_LAYER);" + NL + "\t}" + NL;
   protected final String TEXT_558 = NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected boolean isDirectChild(";
   protected final String TEXT_559 = " childEditPart) {";
   protected final String TEXT_560 = NL + "\t\tif (childEditPart instanceof ";
   protected final String TEXT_561 = ") {" + NL + "\t\t\treturn true;" + NL + "\t\t}";
   protected final String TEXT_562 = NL + "\t\treturn false;" + NL + "\t}";
   protected final String TEXT_563 = NL + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected void addChildVisual(";
   protected final String TEXT_564 = " childEditPart, int index) {";
   protected final String TEXT_565 = NL + "\t\tif (isExternalLabel(childEditPart)) {" + NL + "\t\t\t";
   protected final String TEXT_566 = " labelFigure = ((";
   protected final String TEXT_567 = ") childEditPart).getFigure();" + NL + "\t\t\tgetExternalLabelsContainer().add(labelFigure);" + NL + "\t\t\treturn;" + NL + "\t\t}";
   protected final String TEXT_568 = NL + "\t\tif (addFixedChild(childEditPart)) {" + NL + "\t\t\treturn;" + NL + "\t\t}";
   protected final String TEXT_569 = NL + "\t\tif (isDirectChild(childEditPart)) {" + NL + "\t\t\t";
   protected final String TEXT_570 = " childFigure = ((";
   protected final String TEXT_571 = ") childEditPart).getFigure();" + NL + "\t\t\tchildNodesPane.add(childFigure);" + NL + "\t\t\treturn;" + NL + "\t\t}";
   protected final String TEXT_572 = NL + "\t\tsuper.addChildVisual(childEditPart, -1);" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected void removeChildVisual(";
   protected final String TEXT_573 = " childEditPart) {";
   protected final String TEXT_574 = NL + "\t\tif (isExternalLabel(childEditPart)) {" + NL + "\t\t\t";
   protected final String TEXT_575 = " labelFigure = ((";
   protected final String TEXT_576 = ") childEditPart).getFigure();" + NL + "\t\t\tgetExternalLabelsContainer().remove(labelFigure);" + NL + "\t\t\treturn;" + NL + "\t\t}";
   protected final String TEXT_577 = NL + "\t\tif (removeFixedChild(childEditPart)){" + NL + "\t\t\treturn;" + NL + "\t\t}";
   protected final String TEXT_578 = NL + "\t\tif (isDirectChild(childEditPart)) {" + NL + "\t\t\t";
   protected final String TEXT_579 = " childFigure = ((";
   protected final String TEXT_580 = ") childEditPart).getFigure();" + NL + "\t\t\tchildNodesPane.remove(childFigure);" + NL + "\t\t\treturn;" + NL + "\t\t}";
   protected final String TEXT_581 = NL + "\t\tsuper.removeChildVisual(childEditPart);" + NL + "\t}";
   protected final String TEXT_582 = NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic void removeNotify() {" + NL + "\t\tfor (";
   protected final String TEXT_583 = " it = getChildren().iterator(); it.hasNext();) {" + NL + "\t\t\t";
   protected final String TEXT_584 = " childEditPart = (";
   protected final String TEXT_585 = ") it.next();" + NL + "\t\t\tif (isExternalLabel(childEditPart)) {" + NL + "\t\t\t\tIFigure labelFigure = ((";
   protected final String TEXT_586 = ") childEditPart).getFigure();" + NL + "\t\t\t\tgetExternalLabelsContainer().remove(labelFigure);" + NL + "\t\t\t}" + NL + "\t\t}" + NL + "\t\tsuper.removeNotify();" + NL + "\t}";
   protected final String TEXT_587 = NL + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected void refreshVisuals() {" + NL + "\t\tsuper.refreshVisuals();" + NL + "\t\trefreshBounds();" + NL + "\t\trefreshBackgroundColor();" + NL + "\t\trefreshForegroundColor();" + NL + "\t\trefreshFont();" + NL + "\t\trefreshVisibility();" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected void refreshVisibility() {" + NL + "\t\tboolean isVisible = getDiagramNode().isVisible();" + NL + "\t\tboolean wasVisible = getFigure().isVisible();" + NL + "\t\tif (isVisible == wasVisible) {" + NL + "\t\t\treturn;" + NL + "\t\t}" + NL + "\t\tif (!isVisible && (getSelected() != SELECTED_NONE)) {" + NL + "\t\t\tgetViewer().deselect(this);" + NL + "\t\t}" + NL + "" + NL + "\t\tgetFigure().setVisible(isVisible);" + NL + "\t\tgetFigure().revalidate();" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected void refreshBounds() {" + NL + "\t\t";
   protected final String TEXT_588 = " node = getDiagramNode();" + NL + "\t\tif (node.getLayoutConstraint() == null) {" + NL + "\t\t\treturn;" + NL + "\t\t}" + NL + "\t\tassert node.getLayoutConstraint() instanceof ";
   protected final String TEXT_589 = ";" + NL + "\t\t";
   protected final String TEXT_590 = " bounds = (";
   protected final String TEXT_591 = ") node.getLayoutConstraint();" + NL + "\t\tint x = bounds.getX();" + NL + "\t\tint y = bounds.getY();" + NL + "\t\tint width = bounds.getWidth();" + NL + "\t\tint height = bounds.getHeight();" + NL + "\t\tif (width < 0) {" + NL + "\t\t\tx -= width;" + NL + "\t\t\twidth = -width;" + NL + "\t\t}" + NL + "\t\tif (height < 0) {" + NL + "\t\t\ty -= height;" + NL + "\t\t\theight = -height;" + NL + "\t\t}" + NL + "\t\t((";
   protected final String TEXT_592 = ") getParent()).setLayoutConstraint(this, getFigure(), " + NL + "\t\t\tnew ";
   protected final String TEXT_593 = "(x, y, width, height));" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected ";
   protected final String TEXT_594 = " getModelChildren() {" + NL + "\t\treturn getDiagramNode().getVisibleChildren();" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected ";
   protected final String TEXT_595 = " getModelSourceConnections() {" + NL + "\t\treturn getDiagramNode().getSourceEdges();" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected ";
   protected final String TEXT_596 = " getModelTargetConnections() {" + NL + "\t\treturn getDiagramNode().getTargetEdges();" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic ";
   protected final String TEXT_597 = " getSourceConnectionAnchor(";
   protected final String TEXT_598 = " connection) {" + NL + "\t\treturn new ";
   protected final String TEXT_599 = "(getFigure());" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic ";
   protected final String TEXT_600 = " getSourceConnectionAnchor(";
   protected final String TEXT_601 = " request) {" + NL + "\t\treturn new ";
   protected final String TEXT_602 = "(getFigure());" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic ";
   protected final String TEXT_603 = "  getTargetConnectionAnchor(";
   protected final String TEXT_604 = " connection) {" + NL + "\t\treturn new ";
   protected final String TEXT_605 = "(getFigure());" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic ";
   protected final String TEXT_606 = "  getTargetConnectionAnchor(";
   protected final String TEXT_607 = " request) {" + NL + "\t\treturn new ";
   protected final String TEXT_608 = "(getFigure());" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic Object getAdapter(Class key) {";
   protected final String TEXT_609 = NL + "\t\tif (";
   protected final String TEXT_610 = ".class == key) {" + NL + "\t\t\treturn new ";
   protected final String TEXT_611 = "(this);" + NL + "\t\t}";
   protected final String TEXT_612 = NL + "\t\tif (";
   protected final String TEXT_613 = ".class == key) {" + NL + "\t\t\treturn getTreeEditPartAdapter();" + NL + "\t\t}";
   protected final String TEXT_614 = NL + "\t\treturn super.getAdapter(key);" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate TreeEditPartAdapter myTreeEditPartAdapter;" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate TreeEditPartAdapter getTreeEditPartAdapter() {" + NL + "\t\tif (myTreeEditPartAdapter == null) {" + NL + "\t\t\tmyTreeEditPartAdapter = new TreeEditPartAdapter();" + NL + "\t\t}" + NL + "\t\treturn myTreeEditPartAdapter;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic void activate() {" + NL + "\t\tsuper.activate();" + NL + "\t\tgetDiagramNode().getElement().eAdapters().add(domainModelRefresher);";
   protected final String TEXT_615 = NL + "\t\tinstallNotationModelRefresher();";
   protected final String TEXT_616 = NL + "\t\tinstallLinkNotationModelRefresher();";
   protected final String TEXT_617 = NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic void deactivate() {";
   protected final String TEXT_618 = NL + "\t\tuninstallLinkNotationModelRefresher();";
   protected final String TEXT_619 = NL + "\t\tuninstallNotationModelRefresher();";
   protected final String TEXT_620 = NL + "\t\tgetDiagramNode().getElement().eAdapters().remove(domainModelRefresher);" + NL + "\t\tsuper.deactivate();" + NL + "\t}";
   protected final String TEXT_621 = NL;
   protected final String TEXT_622 = "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate void installNotationModelRefresher() {" + NL + "\t\t";
   protected final String TEXT_623 = " refresher = getNotationModelRefresher();" + NL + "\t\tif (refresher.isInstalled()) {" + NL + "\t\t\treturn;" + NL + "\t\t}" + NL + "\t\t";
   protected final String TEXT_624 = " domainModelEditDomain = ";
   protected final String TEXT_625 = ".getEditingDomain(";
   protected final String TEXT_626 = ".getElement());" + NL + "\t\trefresher.install(domainModelEditDomain);" + NL + "\t\trefreshNotationModel();" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate void uninstallNotationModelRefresher() {" + NL + "\t\tgetNotationModelRefresher().uninstall();" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate ";
   protected final String TEXT_627 = " notationModelRefresher;" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate ";
   protected final String TEXT_628 = " getNotationModelRefresher() {" + NL + "\t\tif (notationModelRefresher == null) {" + NL + "\t\t\tnotationModelRefresher = new NotationModelRefresher();" + NL + "\t\t}" + NL + "\t\treturn notationModelRefresher;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate class NotationModelRefresher extends ";
   protected final String TEXT_629 = " {";
   protected final String TEXT_630 = NL + NL + "\t\t/**" + NL + "\t\t * NB: Children of this element are selected based on constraint declared in ";
   protected final String TEXT_631 = ". " + NL + "\t\t * Since no assumptions may be made concerning the runtime behavior of the constraint, <b>any</b> non-touch notification may result in " + NL + "\t\t * notational model having to be updated." + NL + "\t\t * <p/>User is encouraged to change implementation of this method to provide an optimization if it is safe to assume that not all notifications" + NL + "\t\t * result in such an update." + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprivate ";
   protected final String TEXT_632 = " getConstrainedChildrenFilter() {" + NL + "\t\t\treturn ";
   protected final String TEXT_633 = ".NOT_TOUCH;" + NL + "\t\t}";
   protected final String TEXT_634 = NL + NL + "\t\t/**" + NL + "\t\t * Creates a notification filter which filters notifications that may possibly affect the notational model" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprotected ";
   protected final String TEXT_635 = " createFilter() {";
   protected final String TEXT_636 = NL + "\t\t\t";
   protected final String TEXT_637 = " filter = ";
   protected final String TEXT_638 = ".createFeatureFilter(";
   protected final String TEXT_639 = ".eINSTANCE.get";
   protected final String TEXT_640 = "());";
   protected final String TEXT_641 = NL + "\t\t\tfilter = filter.or(";
   protected final String TEXT_642 = ".createFeatureFilter(";
   protected final String TEXT_643 = ".eINSTANCE.get";
   protected final String TEXT_644 = "()));";
   protected final String TEXT_645 = NL + "\t\t\tfilter = filter.and(";
   protected final String TEXT_646 = ".createNotifierFilter(";
   protected final String TEXT_647 = ".getElement()));";
   protected final String TEXT_648 = NL + "\t\t\t";
   protected final String TEXT_649 = " filter = ";
   protected final String TEXT_650 = ".createNotifierFilter(";
   protected final String TEXT_651 = ".getElement());";
   protected final String TEXT_652 = NL + "\t\t\tfilter = getConstrainedChildrenFilter().or(filter);";
   protected final String TEXT_653 = NL + "\t\t\t";
   protected final String TEXT_654 = " filter = getConstrainedChildrenFilter();";
   protected final String TEXT_655 = NL + "\t\t\t";
   protected final String TEXT_656 = " filter = ";
   protected final String TEXT_657 = ".NOT_TOUCH;";
   protected final String TEXT_658 = NL + "\t\t\treturn filter;";
   protected final String TEXT_659 = NL + "\t\t}" + NL + "" + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprotected ";
   protected final String TEXT_660 = " getCreateNotationalElementCommand(";
   protected final String TEXT_661 = " descriptor) {";
   protected final String TEXT_662 = NL;
   protected final String TEXT_663 = "\t\t\t";
   protected final String TEXT_664 = " domainElement = descriptor.getElement();" + NL + "\t\t\tint nodeVisualID = descriptor.getVisualID();" + NL + "\t\t\tswitch (nodeVisualID) {";
   protected final String TEXT_665 = NL + "\t\t\tcase ";
   protected final String TEXT_666 = ".VISUAL_ID:" + NL + "\t\t\t\tif (domainElement instanceof ";
   protected final String TEXT_667 = ") {" + NL + "\t\t\t\t\treturn new ";
   protected final String TEXT_668 = "(";
   protected final String TEXT_669 = ", domainElement, ";
   protected final String TEXT_670 = "new ";
   protected final String TEXT_671 = "(0, 0, ";
   protected final String TEXT_672 = ", ";
   protected final String TEXT_673 = ")";
   protected final String TEXT_674 = NL + "null";
   protected final String TEXT_675 = ", ";
   protected final String TEXT_676 = ".INSTANCE, false);" + NL + "\t\t\t\t}" + NL + "\t\t\t\treturn null;";
   protected final String TEXT_677 = NL + "\t\t\tdefault:" + NL + "\t\t\t\treturn null;" + NL + "\t\t\t}";
   protected final String TEXT_678 = NL + "\t\t}" + NL + "" + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprotected ";
   protected final String TEXT_679 = " getSemanticChildNodes() {";
   protected final String TEXT_680 = NL;
   protected final String TEXT_681 = NL + "\treturn ";
   protected final String TEXT_682 = ".EMPTY_LIST;";
   protected final String TEXT_683 = NL + "\t";
   protected final String TEXT_684 = " result = new ";
   protected final String TEXT_685 = "();";
   protected final String TEXT_686 = NL + "\t";
   protected final String TEXT_687 = " viewObject = ";
   protected final String TEXT_688 = ";" + NL + "\t";
   protected final String TEXT_689 = " modelObject = viewObject.getElement();" + NL + "\t";
   protected final String TEXT_690 = " nextValue;" + NL + "\tint nodeVID;";
   protected final String TEXT_691 = NL + "\tfor(";
   protected final String TEXT_692 = " it = ";
   protected final String TEXT_693 = ".iterator(); it.hasNext(); ) {" + NL + "\t\tnextValue = (";
   protected final String TEXT_694 = ") it.next();";
   protected final String TEXT_695 = NL + "\tnextValue = (";
   protected final String TEXT_696 = ")";
   protected final String TEXT_697 = ";";
   protected final String TEXT_698 = NL + "\tnodeVID = ";
   protected final String TEXT_699 = ".INSTANCE.getNodeVisualID(viewObject, nextValue);";
   protected final String TEXT_700 = NL + "\tswitch (nodeVID) {";
   protected final String TEXT_701 = NL + "\tcase ";
   protected final String TEXT_702 = ".VISUAL_ID: {";
   protected final String TEXT_703 = NL + "\tif (";
   protected final String TEXT_704 = ".VISUAL_ID == nodeVID) {";
   protected final String TEXT_705 = NL + "\t\tresult.add(new ";
   protected final String TEXT_706 = "(nextValue, nodeVID));";
   protected final String TEXT_707 = NL + "\t\tbreak;" + NL + "\t\t}";
   protected final String TEXT_708 = NL + "\t\t}";
   protected final String TEXT_709 = NL + "\t}";
   protected final String TEXT_710 = NL + "\t}";
   protected final String TEXT_711 = NL + "\treturn result;";
   protected final String TEXT_712 = NL + "\t\t}" + NL + "" + NL + "\t\t/**" + NL + "\t\t * Returns whether a notational element should be created for the given domain element. " + NL + "\t\t * The generated code always returns ";
   protected final String TEXT_713 = ". " + NL + "\t\t * User can change implementation of this method to handle a more sophisticated logic." + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprotected boolean shouldCreateView(";
   protected final String TEXT_714 = " descriptor) {" + NL + "\t\t\treturn ";
   protected final String TEXT_715 = ";" + NL + "\t\t}" + NL + "" + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprotected ";
   protected final String TEXT_716 = " getHost() {" + NL + "\t\t\treturn ";
   protected final String TEXT_717 = ";" + NL + "\t\t}" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected void refreshNotationModel() {" + NL + "\t\t";
   protected final String TEXT_718 = " childRefresher = getNotationModelRefresher();" + NL + "\t\tif (!childRefresher.isInstalled()) {" + NL + "\t\t\treturn;" + NL + "\t\t}" + NL + "\t\t";
   protected final String TEXT_719 = " command = childRefresher.buildRefreshNotationModelCommand();" + NL + "\t\tif (command == null) {" + NL + "\t\t\treturn;" + NL + "\t\t}" + NL + "\t\t";
   protected final String TEXT_720 = " domainModelEditDomain = ";
   protected final String TEXT_721 = ".getEditingDomain(";
   protected final String TEXT_722 = ".getElement());" + NL + "\t\tgetViewer().getEditDomain().getCommandStack().execute(new ";
   protected final String TEXT_723 = "(domainModelEditDomain, command));" + NL + "\t}" + NL;
   protected final String TEXT_724 = NL;
   protected final String TEXT_725 = NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate void installLinkNotationModelRefresher() {" + NL + "\t\tLinkNotationModelRefresher refresher = getLinkNotationModelRefresher();" + NL + "\t\tif (refresher.isInstalled()) {" + NL + "\t\t\treturn;" + NL + "\t\t}" + NL + "\t\t";
   protected final String TEXT_726 = " domainModelEditDomain = ";
   protected final String TEXT_727 = ".getEditingDomain(";
   protected final String TEXT_728 = ".getElement());" + NL + "\t\trefresher.install(domainModelEditDomain);" + NL + "\t\trefreshLinkNotationModel();" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate void uninstallLinkNotationModelRefresher() {" + NL + "\t\tgetLinkNotationModelRefresher().uninstall();" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate LinkNotationModelRefresher linkNotationModelRefresher;" + NL + "" + NL + "\t/**" + NL + "\t * Service to find a notational element that corresponds to the given underlying domain element. " + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate ";
   protected final String TEXT_729 = " viewService;" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate ";
   protected final String TEXT_730 = " getViewService() {" + NL + "\t\tif (viewService == null) {" + NL + "\t\t\tviewService = new ";
   protected final String TEXT_731 = "(getViewer());" + NL + "\t\t}" + NL + "\t\treturn viewService;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate LinkNotationModelRefresher getLinkNotationModelRefresher() {" + NL + "\t\tif (linkNotationModelRefresher == null) {" + NL + "\t\t\tlinkNotationModelRefresher = new LinkNotationModelRefresher(getViewService());" + NL + "\t\t}" + NL + "\t\treturn linkNotationModelRefresher;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate class LinkNotationModelRefresher extends ";
   protected final String TEXT_732 = " {" + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tpublic LinkNotationModelRefresher(";
   protected final String TEXT_733 = " viewService) {" + NL + "\t\t\tsuper(viewService);" + NL + "\t\t}";
   protected final String TEXT_734 = NL + NL + "\t\t/**" + NL + "\t\t * NB: Child links of this element are selected based on constraint declared in ";
   protected final String TEXT_735 = ". " + NL + "\t\t * Since no assumptions may be made concerning the runtime behavior of the constraint, <b>any</b> non-touch notification may result in " + NL + "\t\t * notational model having to be updated." + NL + "\t\t * <p/>User is encouraged to change implementation of this method to provide an optimization if it is safe to assume that not all notifications" + NL + "\t\t * result in such an update." + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprivate ";
   protected final String TEXT_736 = " getConstrainedChildLinksFilter() {" + NL + "\t\t\treturn ";
   protected final String TEXT_737 = ".NOT_TOUCH;" + NL + "\t\t}";
   protected final String TEXT_738 = NL + "\t\t/**" + NL + "\t\t * Creates a notification filter which filters notifications that may possibly result in uncontained links. " + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprivate ";
   protected final String TEXT_739 = " createUncontainedLinksFilter() {" + NL + "\t\t\treturn ";
   protected final String TEXT_740 = ".createEventTypeFilter(";
   protected final String TEXT_741 = ".SET).or(" + NL + "\t\t\t\t";
   protected final String TEXT_742 = ".createEventTypeFilter(";
   protected final String TEXT_743 = ".UNSET).or(" + NL + "\t\t\t\t";
   protected final String TEXT_744 = ".createEventTypeFilter(";
   protected final String TEXT_745 = ".REMOVE).or(" + NL + "\t\t\t\t";
   protected final String TEXT_746 = ".createEventTypeFilter(";
   protected final String TEXT_747 = ".REMOVE_MANY)" + NL + "\t\t\t)));" + NL + "\t\t}";
   protected final String TEXT_748 = NL + "\t\t/**" + NL + "\t\t * Creates a notification filter which filters notifications that may possibly affect the notational model" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprotected ";
   protected final String TEXT_749 = " createFilter() {";
   protected final String TEXT_750 = NL;
   protected final String TEXT_751 = NL + "\t\t\t";
   protected final String TEXT_752 = " filter = ";
   protected final String TEXT_753 = ".createFeatureFilter(";
   protected final String TEXT_754 = ".eINSTANCE.get";
   protected final String TEXT_755 = "());";
   protected final String TEXT_756 = NL + "\t\t\tfilter = filter.or(";
   protected final String TEXT_757 = ".createFeatureFilter(";
   protected final String TEXT_758 = ".eINSTANCE.get";
   protected final String TEXT_759 = "()));";
   protected final String TEXT_760 = NL;
   protected final String TEXT_761 = NL + "\t\t\t";
   protected final String TEXT_762 = " filter = ";
   protected final String TEXT_763 = ".createFeatureFilter(";
   protected final String TEXT_764 = ".eINSTANCE.get";
   protected final String TEXT_765 = "());";
   protected final String TEXT_766 = NL + "\t\t\tfilter = filter.or(";
   protected final String TEXT_767 = ".createFeatureFilter(";
   protected final String TEXT_768 = ".eINSTANCE.get";
   protected final String TEXT_769 = "()));";
   protected final String TEXT_770 = NL;
   protected final String TEXT_771 = NL + "\t\t\t";
   protected final String TEXT_772 = " filter = ";
   protected final String TEXT_773 = ".createFeatureFilter(";
   protected final String TEXT_774 = ".eINSTANCE.get";
   protected final String TEXT_775 = "());";
   protected final String TEXT_776 = NL + "\t\t\tfilter = filter.or(";
   protected final String TEXT_777 = ".createFeatureFilter(";
   protected final String TEXT_778 = ".eINSTANCE.get";
   protected final String TEXT_779 = "()));";
   protected final String TEXT_780 = NL;
   protected final String TEXT_781 = NL + "\t\t\t";
   protected final String TEXT_782 = " filter = ";
   protected final String TEXT_783 = ".createFeatureFilter(";
   protected final String TEXT_784 = ".eINSTANCE.get";
   protected final String TEXT_785 = "());";
   protected final String TEXT_786 = NL + "\t\t\tfilter = filter.or(";
   protected final String TEXT_787 = ".createFeatureFilter(";
   protected final String TEXT_788 = ".eINSTANCE.get";
   protected final String TEXT_789 = "()));";
   protected final String TEXT_790 = NL + "\t\t\tfilter = getConstrainedChildLinksFilter().or(filter);";
   protected final String TEXT_791 = NL + "\t\t\t";
   protected final String TEXT_792 = " filter = getConstrainedChildLinksFilter();";
   protected final String TEXT_793 = NL + "\t\t\tfilter = filter.or(createUncontainedLinksFilter());";
   protected final String TEXT_794 = NL + "\t\t\t";
   protected final String TEXT_795 = " filter = createUncontainedLinksFilter();";
   protected final String TEXT_796 = NL + "\t\t\t";
   protected final String TEXT_797 = " filter = ";
   protected final String TEXT_798 = ".NOT_TOUCH;";
   protected final String TEXT_799 = NL + "\t\t\treturn filter;" + NL + "\t\t}" + NL + "" + NL + "\t\t/**" + NL + "\t \t * @generated" + NL + "\t \t */" + NL + "\t\tprotected ";
   protected final String TEXT_800 = " getCreateNotationalElementCommand(";
   protected final String TEXT_801 = " descriptor) {" + NL + "\t\t\t";
   protected final String TEXT_802 = " linkDescriptor = (";
   protected final String TEXT_803 = ") descriptor;";
   protected final String TEXT_804 = NL;
   protected final String TEXT_805 = "\t\t\t";
   protected final String TEXT_806 = " sourceView = getViewService().findView(linkDescriptor.getSource());" + NL + "\t\t\t";
   protected final String TEXT_807 = " targetView = getViewService().findView(linkDescriptor.getDestination());" + NL + "\t\t\t";
   protected final String TEXT_808 = " decorator = null;" + NL + "\t\t\tif (sourceView == null || targetView == null) {" + NL + "\t\t\t\treturn null;" + NL + "\t\t\t}" + NL + "\t\t\tswitch (linkDescriptor.getVisualID()) {";
   protected final String TEXT_809 = NL + "\t\t\tcase ";
   protected final String TEXT_810 = ".VISUAL_ID:" + NL + "\t\t\t\tif (linkDescriptor.getElement() instanceof ";
   protected final String TEXT_811 = ") {" + NL + "\t\t\t\t\tdecorator = ";
   protected final String TEXT_812 = ".INSTANCE;" + NL + "\t\t\t\t}" + NL + "\t\t\t\tbreak;";
   protected final String TEXT_813 = NL + "\t\t\tcase ";
   protected final String TEXT_814 = ".VISUAL_ID:" + NL + "\t\t\t\tif (linkDescriptor.getElement() == null) {" + NL + "\t\t\t\t\tdecorator = ";
   protected final String TEXT_815 = ".INSTANCE;" + NL + "\t\t\t\t}" + NL + "\t\t\t\tbreak;";
   protected final String TEXT_816 = NL + "\t\t\t}" + NL + "\t\t\tif (decorator == null) {" + NL + "\t\t\t\treturn null;" + NL + "\t\t\t}" + NL + "\t\t\treturn new ";
   protected final String TEXT_817 = "(";
   protected final String TEXT_818 = ", linkDescriptor.getElement(), sourceView, targetView, decorator);";
   protected final String TEXT_819 = NL + "\t\t}" + NL + "" + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprotected ";
   protected final String TEXT_820 = " getSemanticChildLinks() {";
   protected final String TEXT_821 = NL;
   protected final String TEXT_822 = "\t";
   protected final String TEXT_823 = " result = new ";
   protected final String TEXT_824 = "();";
   protected final String TEXT_825 = NL + "\t";
   protected final String TEXT_826 = " modelObject = ";
   protected final String TEXT_827 = ".getElement();" + NL + "\t";
   protected final String TEXT_828 = " nextValue;";
   protected final String TEXT_829 = NL + "\tint linkVID;";
   protected final String TEXT_830 = NL + "\tfor(";
   protected final String TEXT_831 = " it = ";
   protected final String TEXT_832 = ".iterator(); it.hasNext(); ) {" + NL + "\t\tnextValue = (";
   protected final String TEXT_833 = ") it.next();";
   protected final String TEXT_834 = NL + "\tnextValue = (";
   protected final String TEXT_835 = ")";
   protected final String TEXT_836 = ";";
   protected final String TEXT_837 = NL + "\tlinkVID = ";
   protected final String TEXT_838 = ".INSTANCE.getLinkWithClassVisualID(nextValue);";
   protected final String TEXT_839 = NL + "\tswitch (linkVID) {";
   protected final String TEXT_840 = NL + "\tcase ";
   protected final String TEXT_841 = ".VISUAL_ID: {";
   protected final String TEXT_842 = NL + "\tif (";
   protected final String TEXT_843 = ".VISUAL_ID == linkVID) {";
   protected final String TEXT_844 = NL + "\t\t";
   protected final String TEXT_845 = " source = (";
   protected final String TEXT_846 = ")";
   protected final String TEXT_847 = ";";
   protected final String TEXT_848 = NL + "\t\t";
   protected final String TEXT_849 = " source = ";
   protected final String TEXT_850 = ".getElement();";
   protected final String TEXT_851 = NL + "\t\t";
   protected final String TEXT_852 = " target = (";
   protected final String TEXT_853 = ")";
   protected final String TEXT_854 = ";";
   protected final String TEXT_855 = NL + "\t\t";
   protected final String TEXT_856 = " target = ";
   protected final String TEXT_857 = ".getElement();";
   protected final String TEXT_858 = NL + "\t\tif (source != null && target != null) {" + NL + "\t\t\tresult.add(new ";
   protected final String TEXT_859 = "(source, target, nextValue, linkVID));" + NL + "\t\t}";
   protected final String TEXT_860 = NL + "\t\tbreak;" + NL + "\t}";
   protected final String TEXT_861 = NL + "\t}";
   protected final String TEXT_862 = NL + "\t}";
   protected final String TEXT_863 = NL + "\t}";
   protected final String TEXT_864 = NL + "\tfor(";
   protected final String TEXT_865 = " it = ";
   protected final String TEXT_866 = ".iterator(); it.hasNext(); ) {" + NL + "\t\tnextValue = (";
   protected final String TEXT_867 = ") it.next();";
   protected final String TEXT_868 = NL + "\tnextValue = (";
   protected final String TEXT_869 = ")";
   protected final String TEXT_870 = ";";
   protected final String TEXT_871 = NL + "\tif (nextValue != null) {";
   protected final String TEXT_872 = NL + "\t\tresult.add(new ";
   protected final String TEXT_873 = "(modelObject, nextValue, null, ";
   protected final String TEXT_874 = ".VISUAL_ID));";
   protected final String TEXT_875 = NL + "\t}";
   protected final String TEXT_876 = NL + "\t}";
   protected final String TEXT_877 = NL + "\treturn result;";
   protected final String TEXT_878 = NL + "\t\t}" + NL + "" + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprotected ";
   protected final String TEXT_879 = " getNotationalChildLinks() {" + NL + "\t\t\t";
   protected final String TEXT_880 = " result = new ";
   protected final String TEXT_881 = "();" + NL + "\t\t\t";
   protected final String TEXT_882 = " allLinks = ";
   protected final String TEXT_883 = ".getEdges();" + NL + "\t\t\tfor(";
   protected final String TEXT_884 = " it = allLinks.iterator(); it.hasNext(); ) {" + NL + "\t\t\t\t";
   protected final String TEXT_885 = " next = (";
   protected final String TEXT_886 = ") it.next();";
   protected final String TEXT_887 = NL + "\t\t\t\tif (next.isSetElement() && next.getElement() != null && next.getElement().eResource() == null) {" + NL + "\t\t\t\t\tresult.add(next);" + NL + "\t\t\t\t\tcontinue;" + NL + "\t\t\t\t}" + NL + "\t\t\t\t";
   protected final String TEXT_888 = " source = next.getSource();" + NL + "\t\t\t\tif (source == null || (source.isSetElement() && source.getElement() != null && source.getElement().eResource() == null)) {" + NL + "\t\t\t\t\tresult.add(next);" + NL + "\t\t\t\t\tcontinue;" + NL + "\t\t\t\t}" + NL + "\t\t\t\t";
   protected final String TEXT_889 = " target = next.getTarget();" + NL + "\t\t\t\tif (target == null || (target.isSetElement() && target.getElement() != null && target.getElement().eResource() == null)) {" + NL + "\t\t\t\t\tresult.add(next);" + NL + "\t\t\t\t\tcontinue;" + NL + "\t\t\t\t}";
   protected final String TEXT_890 = NL + "\t\t\t\tif (!next.isSetElement() || next.getElement() == null) {" + NL + "\t\t\t\t\tif (next.getSource() == ";
   protected final String TEXT_891 = ") {" + NL + "\t\t\t\t\t\tint linkVID = ";
   protected final String TEXT_892 = ".getVisualID(next);" + NL + "\t\t\t\t\t\tswitch (linkVID) {";
   protected final String TEXT_893 = NL + "\t\t\t\t\t\tcase ";
   protected final String TEXT_894 = ".VISUAL_ID:";
   protected final String TEXT_895 = NL + "\t\t\t\t\t\t\tresult.add(next);" + NL + "\t\t\t\t\t\t\tbreak;" + NL + "\t\t\t\t\t\t}" + NL + "\t\t\t\t\t}";
   protected final String TEXT_896 = NL + "\t\t\t\t} else {";
   protected final String TEXT_897 = NL + "\t\t\t\t}";
   protected final String TEXT_898 = NL + "\t\t\t\tif (next.isSetElement() && next.getElement() != null) {";
   protected final String TEXT_899 = NL + "\t\t\t\t\tif (next.getElement().eContainer() == ";
   protected final String TEXT_900 = ".getElement()) {" + NL + "\t\t\t\t\t\tint linkVID = ";
   protected final String TEXT_901 = ".getVisualID(next);" + NL + "\t\t\t\t\t\tswitch (linkVID) {";
   protected final String TEXT_902 = NL + "\t\t\t\t\t\tcase ";
   protected final String TEXT_903 = ".VISUAL_ID:";
   protected final String TEXT_904 = NL + "\t\t\t\t\t\t\tresult.add(next);" + NL + "\t\t\t\t\t\t\tbreak;" + NL + "\t\t\t\t\t\t}" + NL + "\t\t\t\t\t}" + NL + "\t\t\t\t}";
   protected final String TEXT_905 = NL + "\t\t\t}" + NL + "\t\t\treturn result;" + NL + "\t\t}" + NL + "" + NL + "\t\t/**" + NL + "\t\t * Returns whether a notational edge should be created for the given domain element. " + NL + "\t\t * The generated code always returns ";
   protected final String TEXT_906 = ". " + NL + "\t\t * User can change implementation of this method to handle a more sophisticated logic." + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprotected boolean shouldCreateView(";
   protected final String TEXT_907 = " descriptor) {" + NL + "\t\t\treturn ";
   protected final String TEXT_908 = ";" + NL + "\t\t}" + NL + "" + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprotected ";
   protected final String TEXT_909 = " getHost() {" + NL + "\t\t\treturn ";
   protected final String TEXT_910 = ";" + NL + "\t\t}" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected void refreshLinkNotationModel() {" + NL + "\t\t";
   protected final String TEXT_911 = " linkRefresher = getLinkNotationModelRefresher();" + NL + "\t\tif (!linkRefresher.isInstalled()) {" + NL + "\t\t\treturn;" + NL + "\t\t}" + NL + "\t\t";
   protected final String TEXT_912 = " command = linkRefresher.buildRefreshNotationModelCommand();" + NL + "\t\tif (command == null) {" + NL + "\t\t\treturn;" + NL + "\t\t}" + NL + "\t\t";
   protected final String TEXT_913 = " domainModelEditDomain = ";
   protected final String TEXT_914 = ".getEditingDomain(";
   protected final String TEXT_915 = ".getElement());" + NL + "\t\tgetViewer().getEditDomain().getCommandStack().execute(new ";
   protected final String TEXT_916 = "(domainModelEditDomain, command));" + NL + "\t}";
   protected final String TEXT_917 = NL;
   protected final String TEXT_918 = NL;
   protected final String TEXT_919 = "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate ";
   protected final String TEXT_920 = " domainModelRefresher = new ";
   protected final String TEXT_921 = "(this);" + NL;
   protected final String TEXT_922 = NL;
   protected final String TEXT_923 = "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic void addRefresher(";
   protected final String TEXT_924 = " feature, Refresher refresher) {" + NL + "\t\tCompositeRefresher compositeRefresher = getCompositeRefresher(feature);" + NL + "\t\tcompositeRefresher.addRefresher(refresher);" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic void removeRefresher(";
   protected final String TEXT_925 = " feature, Refresher refresher) {" + NL + "\t\tCompositeRefresher compositeRefresher = getCompositeRefresher(feature);" + NL + "\t\tcompositeRefresher.removeRefresher(refresher);" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate CompositeRefresher getCompositeRefresher(";
   protected final String TEXT_926 = " feature) {" + NL + "\t\tif (structuralFeatures2Refresher == null) {" + NL + "\t\t\tcreateRefreshers();" + NL + "\t\t}" + NL + "\t\tRefresher refresher = (Refresher) structuralFeatures2Refresher.get(feature);" + NL + "\t\tif (refresher instanceof CompositeRefresher) {" + NL + "\t\t\treturn (CompositeRefresher) refresher;" + NL + "\t\t}" + NL + "\t\tCompositeRefresher result = new CompositeRefresher();" + NL + "\t\tif (refresher != null) {" + NL + "\t\t\tresult.addRefresher(refresher);" + NL + "\t\t}" + NL + "\t\tstructuralFeatures2Refresher.put(feature, result);" + NL + "\t\treturn result;" + NL + "\t}" + NL;
   protected final String TEXT_927 = NL;
   protected final String TEXT_928 = NL + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected void refreshFont() {";
   protected final String TEXT_929 = NL + "\t\t";
   protected final String TEXT_930 = " style =" + NL + "\t\t\t(";
   protected final String TEXT_931 = ") ";
   protected final String TEXT_932 = ".getStyle(" + NL + "\t\t\t\t";
   protected final String TEXT_933 = ".eINSTANCE.getFontStyle());" + NL + "\t\t";
   protected final String TEXT_934 = " toDispose = createdFont;" + NL + "\t\tif (style != null) {" + NL + "\t\t\tString fontName = style.getFontName();" + NL + "\t\t\tint fontHeight = style.getFontHeight();" + NL + "\t\t\tint fontStyle = ";
   protected final String TEXT_935 = ".NORMAL;" + NL + "\t\t\tif (style.isBold()) {" + NL + "\t\t\t\tfontStyle |= ";
   protected final String TEXT_936 = ".BOLD;" + NL + "\t\t\t}" + NL + "\t\t\tif (style.isItalic()) {" + NL + "\t\t\t\tfontStyle |= ";
   protected final String TEXT_937 = ".ITALIC;" + NL + "\t\t\t}" + NL + "\t\t\t";
   protected final String TEXT_938 = " currentFont = getFigure().getFont();" + NL + "\t\t\tif (currentFont != null) {" + NL + "\t\t\t\t";
   protected final String TEXT_939 = " currentFontData = currentFont.getFontData()[0];" + NL + "\t\t\t\tif (currentFontData.getName().equals(fontName) && currentFontData.getHeight() == fontHeight && currentFontData.getStyle() == fontStyle) {" + NL + "\t\t\t\t\treturn;" + NL + "\t\t\t\t}" + NL + "\t\t\t}" + NL + "\t\t\tcreatedFont = new ";
   protected final String TEXT_940 = "(null, fontName, fontHeight, fontStyle);" + NL + "\t\t\tgetFigure().setFont(createdFont);" + NL + "\t\t} else {" + NL + "\t\t\t//revert to the default font" + NL + "\t\t\tgetFigure().setFont(getViewer().getControl().getFont());" + NL + "\t\t\tcreatedFont = null;" + NL + "\t\t}" + NL + "\t\tif (toDispose != null) {" + NL + "\t\t\ttoDispose.dispose();" + NL + "\t\t}";
   protected final String TEXT_941 = "\t" + NL + "\t}";
   protected final String TEXT_942 = NL + NL + "\t/**" + NL + "\t * The font (created by {@link #refreshFont()}) currently assigned to the label (unless the default font is assigned)." + NL + "\t * Whenever another non-default font is assigned to it, it is safe to dispose the previous one." + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate ";
   protected final String TEXT_943 = " createdFont;";
   protected final String TEXT_944 = "\t" + NL;
   protected final String TEXT_945 = NL;
   protected final String TEXT_946 = "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected void refreshForegroundColor() {" + NL + "\t\t";
   protected final String TEXT_947 = " style = (";
   protected final String TEXT_948 = ")  ";
   protected final String TEXT_949 = ".getStyle(";
   protected final String TEXT_950 = ".eINSTANCE.getLineStyle());" + NL + "\t\t";
   protected final String TEXT_951 = " toDispose = createdForegroundColor;" + NL + "\t\tif (style != null) {" + NL + "\t\t\tint foregroundColor = style.getLineColor();" + NL + "\t\t\tint red = foregroundColor & 0x000000FF;" + NL + "\t\t\tint green = (foregroundColor & 0x0000FF00) >> 8;" + NL + "\t\t\tint blue = (foregroundColor & 0x00FF0000) >> 16;" + NL + "\t\t\t";
   protected final String TEXT_952 = " currentColor = getFigure().getForegroundColor();" + NL + "\t\t\tif (currentColor != null && currentColor.getRed() == red && currentColor.getGreen() == green && currentColor.getBlue() == blue) {" + NL + "\t\t\t\treturn;" + NL + "\t\t\t}" + NL + "\t\t\tcreatedForegroundColor = new ";
   protected final String TEXT_953 = "(null, red, green, blue);" + NL + "\t\t\tgetFigure().setForegroundColor(createdForegroundColor);" + NL + "\t\t} else {" + NL + "\t\t\tgetFigure().setForegroundColor(getViewer().getControl().getForeground());" + NL + "\t\t\tcreatedForegroundColor = null;" + NL + "\t\t}" + NL + "\t\tif (toDispose != null) {" + NL + "\t\t\ttoDispose.dispose();" + NL + "\t\t}" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * The color (created by {@link #refreshForegroundColor()}) currently assigned to the figure." + NL + "\t * Whenever another color is assigned to it, it is safe to dispose the previous one." + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate ";
   protected final String TEXT_954 = " createdForegroundColor;" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected void refreshBackgroundColor() {" + NL + "\t\t";
   protected final String TEXT_955 = " style = (";
   protected final String TEXT_956 = ")  ";
   protected final String TEXT_957 = ".getStyle(";
   protected final String TEXT_958 = ".eINSTANCE.getFillStyle());" + NL + "\t\t";
   protected final String TEXT_959 = " toDispose = createdBackgroundColor;" + NL + "\t\tif (style != null) {" + NL + "\t\t\tint backgroundColor = style.getFillColor();" + NL + "\t\t\tint red = backgroundColor & 0x000000FF;" + NL + "\t\t\tint green = (backgroundColor & 0x0000FF00) >> 8;" + NL + "\t\t\tint blue = (backgroundColor & 0x00FF0000) >> 16;" + NL + "\t\t\t";
   protected final String TEXT_960 = " currentColor = getFigure().getBackgroundColor();" + NL + "\t\t\tif (currentColor != null && currentColor.getRed() == red && currentColor.getGreen() == green && currentColor.getBlue() == blue) {" + NL + "\t\t\t\treturn;" + NL + "\t\t\t}" + NL + "\t\t\tcreatedBackgroundColor = new ";
   protected final String TEXT_961 = "(null, red, green, blue);" + NL + "\t\t\tgetFigure().setBackgroundColor(createdBackgroundColor);" + NL + "\t\t} else {" + NL + "\t\t\tgetFigure().setBackgroundColor(getViewer().getControl().getBackground());" + NL + "\t\t}" + NL + "\t\tif (toDispose != null) {" + NL + "\t\t\ttoDispose.dispose();" + NL + "\t\t}" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * The color (created by {@link #refreshBackgroundColor()}) currently assigned to the figure." + NL + "\t * Whenever another color is assigned to it, it is safe to dispose the previous one." + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate ";
   protected final String TEXT_962 = " createdBackgroundColor;" + NL;
   protected final String TEXT_963 = NL;
   protected final String TEXT_964 = "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate ";
   protected final String TEXT_965 = " structuralFeatures2Refresher;" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic Refresher getRefresher(";
   protected final String TEXT_966 = " feature, ";
   protected final String TEXT_967 = " msg) {" + NL + "\t\tif (structuralFeatures2Refresher == null) {" + NL + "\t\t\tcreateRefreshers();" + NL + "\t\t}" + NL + "\t\treturn (Refresher) structuralFeatures2Refresher.get(feature);" + NL + "\t}" + NL + "" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate void createRefreshers() {" + NL + "\t\tstructuralFeatures2Refresher = new ";
   protected final String TEXT_968 = "();";
   protected final String TEXT_969 = NL;
   protected final String TEXT_970 = "\t\tRefresher childrenRefresher = new Refresher() {" + NL + "\t\t\tpublic void refresh() {" + NL + "\t\t\t\trefreshChildren();" + NL + "\t\t\t}" + NL + "\t\t};" + NL + "\t\tstructuralFeatures2Refresher.put(";
   protected final String TEXT_971 = ".eINSTANCE.getView_PersistedChildren(), childrenRefresher);" + NL + "\t\tstructuralFeatures2Refresher.put(";
   protected final String TEXT_972 = ".eINSTANCE.getView_TransientChildren(), childrenRefresher);" + NL + "\t\tstructuralFeatures2Refresher.put(";
   protected final String TEXT_973 = ".eINSTANCE.getView_Styles(), childrenRefresher);" + NL + "\t\tstructuralFeatures2Refresher.put(";
   protected final String TEXT_974 = ".eINSTANCE.getDrawerStyle_Collapsed(), childrenRefresher);" + NL + "\t\t";
   protected final String TEXT_975 = NL;
   protected final String TEXT_976 = "\t\tRefresher boundsRefresher = new Refresher() {" + NL + "\t\t\tpublic void refresh() {" + NL + "\t\t\t\trefreshBounds();" + NL + "\t\t\t}" + NL + "\t\t};" + NL + "\t\tstructuralFeatures2Refresher.put(";
   protected final String TEXT_977 = ".eINSTANCE.getNode_LayoutConstraint(), boundsRefresher);" + NL + "\t\tstructuralFeatures2Refresher.put(";
   protected final String TEXT_978 = ".eINSTANCE.getSize_Width(), boundsRefresher);" + NL + "\t\tstructuralFeatures2Refresher.put(";
   protected final String TEXT_979 = ".eINSTANCE.getSize_Height(), boundsRefresher);" + NL + "\t\tstructuralFeatures2Refresher.put(";
   protected final String TEXT_980 = ".eINSTANCE.getLocation_X(), boundsRefresher);" + NL + "\t\tstructuralFeatures2Refresher.put(";
   protected final String TEXT_981 = ".eINSTANCE.getLocation_Y(), boundsRefresher);";
   protected final String TEXT_982 = NL;
   protected final String TEXT_983 = "\t\tRefresher visibilityRefresher = new Refresher() {" + NL + "\t\t\tpublic void refresh() {" + NL + "\t\t\t\trefreshVisibility();" + NL + "\t\t\t}" + NL + "\t\t};" + NL + "\t\tstructuralFeatures2Refresher.put(";
   protected final String TEXT_984 = ".eINSTANCE.getView_Visible(), visibilityRefresher);";
   protected final String TEXT_985 = NL;
   protected final String TEXT_986 = "\t\tRefresher sourceEdgesRefresher = new Refresher() {" + NL + "\t\t\tpublic void refresh() {" + NL + "\t\t\t\trefreshSourceConnections();" + NL + "\t\t\t}" + NL + "\t\t};" + NL + "\t\tstructuralFeatures2Refresher.put(";
   protected final String TEXT_987 = ".eINSTANCE.getView_SourceEdges(), sourceEdgesRefresher);";
   protected final String TEXT_988 = NL;
   protected final String TEXT_989 = "\t\tRefresher targetEdgesRefresher = new Refresher() {" + NL + "\t\t\tpublic void refresh() {" + NL + "\t\t\t\trefreshTargetConnections();" + NL + "\t\t\t}" + NL + "\t\t};" + NL + "\t\tstructuralFeatures2Refresher.put(";
   protected final String TEXT_990 = ".eINSTANCE.getView_TargetEdges(), targetEdgesRefresher);";
   protected final String TEXT_991 = NL;
   protected final String TEXT_992 = "\t\tRefresher fontRefresher = new Refresher() {" + NL + "\t\t\tpublic void refresh() {" + NL + "\t\t\t\trefreshFont();" + NL + "\t\t\t}" + NL + "\t\t};" + NL + "\t\tstructuralFeatures2Refresher.put(";
   protected final String TEXT_993 = ".eINSTANCE.getFontStyle_FontHeight(), fontRefresher);" + NL + "\t\tstructuralFeatures2Refresher.put(";
   protected final String TEXT_994 = ".eINSTANCE.getFontStyle_FontName(), fontRefresher);" + NL + "\t\tstructuralFeatures2Refresher.put(";
   protected final String TEXT_995 = ".eINSTANCE.getFontStyle_Bold(), fontRefresher);" + NL + "\t\tstructuralFeatures2Refresher.put(";
   protected final String TEXT_996 = ".eINSTANCE.getFontStyle_Italic(), fontRefresher);" + NL + "\t\t";
   protected final String TEXT_997 = NL;
   protected final String TEXT_998 = "\t\tRefresher backgroundColorRefresher = new Refresher() {" + NL + "\t\t\tpublic void refresh() {" + NL + "\t\t\t\trefreshBackgroundColor();" + NL + "\t\t\t}" + NL + "\t\t};" + NL + "\t\tstructuralFeatures2Refresher.put(";
   protected final String TEXT_999 = ".eINSTANCE.getFillStyle_FillColor(), backgroundColorRefresher);" + NL + "\t\tRefresher foregroundColorRefresher = new Refresher() {" + NL + "\t\t\tpublic void refresh() {" + NL + "\t\t\t\trefreshForegroundColor();" + NL + "\t\t\t}" + NL + "\t\t};" + NL + "\t\tstructuralFeatures2Refresher.put(";
   protected final String TEXT_1000 = ".eINSTANCE.getLineStyle_LineColor(), foregroundColorRefresher);" + NL + "\t}" + NL;
   protected final String TEXT_1001 = NL;
   protected final String TEXT_1002 = NL;
   protected final String TEXT_1003 = NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate static class MapModeWorkaround {" + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tpublic int DPtoLP(int dp) {" + NL + "\t\t\treturn dp;" + NL + "\t\t}" + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tpublic static MapModeWorkaround INSTANCE = new MapModeWorkaround();" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate MapModeWorkaround getMapMode() {" + NL + "\t\treturn MapModeWorkaround.INSTANCE;" + NL + "\t}";
   protected final String TEXT_1004 = NL + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate class TreeEditPartAdapter extends ";
   protected final String TEXT_1005 = " {" + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tpublic TreeEditPartAdapter() {" + NL + "\t\t\tsuper(getDiagramNode(), ";
   protected final String TEXT_1006 = ".getInstance().getItemProvidersAdapterFactory());" + NL + "\t\t}" + NL + "" + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprotected void createEditPolicies() {";
   protected final String TEXT_1007 = NL;
   protected final String TEXT_1008 = "\t\tinstallEditPolicy(";
   protected final String TEXT_1009 = ".COMPONENT_ROLE, new ";
   protected final String TEXT_1010 = "() {" + NL + "\t\t\tprotected ";
   protected final String TEXT_1011 = " createDeleteCommand(";
   protected final String TEXT_1012 = " deleteRequest) {" + NL + "\t\t\t\t";
   protected final String TEXT_1013 = " editingDomain = ";
   protected final String TEXT_1014 = ".getEditingDomain(getDiagramNode().getDiagram().getElement());" + NL + "\t\t\t\t";
   protected final String TEXT_1015 = " cc = new ";
   protected final String TEXT_1016 = "();" + NL + "\t\t\t\tcc.append(getDomainModelRemoveCommand(editingDomain));" + NL + "\t\t\t\tcc.append(new ";
   protected final String TEXT_1017 = "((";
   protected final String TEXT_1018 = ") getDiagramNode().eContainer(), getDiagramNode()));" + NL + "\t\t\t\treturn new ";
   protected final String TEXT_1019 = "(editingDomain, cc);" + NL + "\t\t\t}" + NL + "" + NL + "\t\t\tprivate org.eclipse.emf.common.command.Command getDomainModelRemoveCommand(";
   protected final String TEXT_1020 = " editingDomain) {";
   protected final String TEXT_1021 = NL + "\t\t\t\t";
   protected final String TEXT_1022 = " result = new ";
   protected final String TEXT_1023 = "();";
   protected final String TEXT_1024 = NL + "\t\t\t\tresult.append(";
   protected final String TEXT_1025 = ".create(" + NL + "\t\t\t\t\teditingDomain, " + NL + "\t\t\t\t\tgetDiagramNode().getElement().eContainer(), ";
   protected final String TEXT_1026 = ".eINSTANCE.get";
   protected final String TEXT_1027 = "()," + NL + "\t\t\t\t\tgetDiagramNode().getElement()));";
   protected final String TEXT_1028 = NL + "\t\t\t\tresult.append(";
   protected final String TEXT_1029 = ".create(" + NL + "\t\t\t\t\teditingDomain, " + NL + "\t\t\t\t\tgetDiagramNode().getElement().eContainer(), ";
   protected final String TEXT_1030 = ".eINSTANCE.get";
   protected final String TEXT_1031 = "()," + NL + "\t\t\t\t\t";
   protected final String TEXT_1032 = ".UNSET_VALUE));";
   protected final String TEXT_1033 = NL + "\t\t\t\tresult.append(";
   protected final String TEXT_1034 = ".create(" + NL + "\t\t\t\t\teditingDomain, " + NL + "\t\t\t\t\tgetDiagramNode().getElement().eContainer(), ";
   protected final String TEXT_1035 = ".eINSTANCE.get";
   protected final String TEXT_1036 = "()," + NL + "\t\t\t\t\tgetDiagramNode().getElement()));";
   protected final String TEXT_1037 = NL + "\t\t\t\tresult.append(";
   protected final String TEXT_1038 = ".create(" + NL + "\t\t\t\t\teditingDomain, " + NL + "\t\t\t\t\tgetDiagramNode().getElement().eContainer(), ";
   protected final String TEXT_1039 = ".eINSTANCE.get";
   protected final String TEXT_1040 = "()," + NL + "\t\t\t\t\t";
   protected final String TEXT_1041 = ".UNSET_VALUE));";
   protected final String TEXT_1042 = NL + "\t\t\t\treturn result;";
   protected final String TEXT_1043 = NL + "\t\t\t\treturn ";
   protected final String TEXT_1044 = ".create(" + NL + "\t\t\t\t\teditingDomain, " + NL + "\t\t\t\t\tgetDiagramNode().getElement().eContainer(), ";
   protected final String TEXT_1045 = ".eINSTANCE.get";
   protected final String TEXT_1046 = "()," + NL + "\t\t\t\t\tgetDiagramNode().getElement());";
   protected final String TEXT_1047 = NL + "\t\t\t\treturn ";
   protected final String TEXT_1048 = ".create(" + NL + "\t\t\t\t\teditingDomain, " + NL + "\t\t\t\t\tgetDiagramNode().getElement().eContainer(), ";
   protected final String TEXT_1049 = ".eINSTANCE.get";
   protected final String TEXT_1050 = "()," + NL + "\t\t\t\t\t";
   protected final String TEXT_1051 = ".UNSET_VALUE);";
   protected final String TEXT_1052 = NL + "\t\t\t}" + NL + "\t\t});";
   protected final String TEXT_1053 = NL;
   protected final String TEXT_1054 = "\t\tinstallEditPolicy(";
   protected final String TEXT_1055 = ".DIRECT_EDIT_ROLE," + NL + "\t\t\t\tnew ";
   protected final String TEXT_1056 = "() {" + NL + "\t\t\t\t\tprotected void showCurrentEditValue(";
   protected final String TEXT_1057 = " request) {" + NL + "\t\t\t\t\t\tString value = (String) request.getCellEditor().getValue();" + NL + "\t\t\t\t\t\tsetWidgetText(value);" + NL + "\t\t\t\t\t}";
   protected final String TEXT_1058 = NL;
   protected final String TEXT_1059 = "\t\t\t\t\tprotected ";
   protected final String TEXT_1060 = " getDirectEditCommand(";
   protected final String TEXT_1061 = " request) {";
   protected final String TEXT_1062 = NL + "\t\t\t\t\t\treturn ";
   protected final String TEXT_1063 = ".INSTANCE;";
   protected final String TEXT_1064 = NL + "\t\t\t\t\t\tString value = (String) request.getCellEditor().getValue();" + NL + "\t\t\t\t\t\tif (value == null) {" + NL + "\t\t\t\t\t\t\t//Invalid value is transformed into a null by the validator." + NL + "\t\t\t\t\t\t\t//XXX: implement validator" + NL + "\t\t\t\t\t\t\treturn ";
   protected final String TEXT_1065 = ".INSTANCE;" + NL + "\t\t\t\t\t\t}" + NL + "\t\t\t\t\t\tfinal Object[] parseResult;";
   protected final String TEXT_1066 = NL + "\t\t\t\t\t\tif (value.length() == 0) {" + NL + "\t\t\t\t\t\t\tparseResult = new Object[] { null };" + NL + "\t\t\t\t\t\t} else {";
   protected final String TEXT_1067 = NL + "\t\t\t\t\t\ttry {" + NL + "\t\t\t\t\t\t\tparseResult = new ";
   protected final String TEXT_1068 = "(";
   protected final String TEXT_1069 = ").parse(value);" + NL + "\t\t\t\t\t\t} catch (IllegalArgumentException e) {" + NL + "\t\t\t\t\t\t\treturn ";
   protected final String TEXT_1070 = ".INSTANCE;" + NL + "\t\t\t\t\t\t} catch (";
   protected final String TEXT_1071 = " e) {" + NL + "\t\t\t\t\t\t\treturn ";
   protected final String TEXT_1072 = ".INSTANCE;" + NL + "\t\t\t\t\t\t}";
   protected final String TEXT_1073 = NL + "\t\t\t\t\t\t}";
   protected final String TEXT_1074 = NL + "\t\t\t\t\t\t";
   protected final String TEXT_1075 = " editingDomain = ";
   protected final String TEXT_1076 = ".getEditingDomain(";
   protected final String TEXT_1077 = ".getDiagram().getElement());" + NL + "\t\t\t\t\t\tif (parseResult.length != ";
   protected final String TEXT_1078 = ") {" + NL + "\t\t\t\t\t\t\treturn ";
   protected final String TEXT_1079 = ".INSTANCE;" + NL + "\t\t\t\t\t\t}" + NL + "\t\t\t\t\t\t";
   protected final String TEXT_1080 = " domainModelCommand = createDomainModelCommand(editingDomain, parseResult);" + NL + "\t\t\t\t\t\treturn new ";
   protected final String TEXT_1081 = "(editingDomain, domainModelCommand);" + NL + "\t\t\t\t\t}" + NL + "\t\t\t\t\t";
   protected final String TEXT_1082 = " createDomainModelCommand(";
   protected final String TEXT_1083 = " editingDomain, Object[] values) {" + NL + "\t\t\t\t\t\t";
   protected final String TEXT_1084 = " element = ";
   protected final String TEXT_1085 = ";" + NL + "\t\t\t\t\t\t";
   protected final String TEXT_1086 = " result = new ";
   protected final String TEXT_1087 = "();" + NL + "\t\t\t\t\t\tObject valueToSet;";
   protected final String TEXT_1088 = NL + "\t\t\t\t\t\t";
   protected final String TEXT_1089 = " ";
   protected final String TEXT_1090 = "feature = (";
   protected final String TEXT_1091 = ") ";
   protected final String TEXT_1092 = ".eINSTANCE.get";
   protected final String TEXT_1093 = "();" + NL + "\t\t\t\t\t\ttry {" + NL + "\t\t\t\t\t\t\tvalueToSet = ";
   protected final String TEXT_1094 = ".parseValue(feature, values[";
   protected final String TEXT_1095 = "]);" + NL + "\t\t\t\t\t\t} catch (IllegalArgumentException e) {" + NL + "\t\t\t\t\t\t\treturn ";
   protected final String TEXT_1096 = ".INSTANCE;" + NL + "\t\t\t\t\t\t}";
   protected final String TEXT_1097 = NL + "\t\t\t\t\t\t";
   protected final String TEXT_1098 = " ";
   protected final String TEXT_1099 = "values = new ";
   protected final String TEXT_1100 = "();" + NL + "\t\t\t\t\t\tvalues.addAll(element.get";
   protected final String TEXT_1101 = "());" + NL + "\t\t\t\t\t\tresult.append(";
   protected final String TEXT_1102 = ".create(editingDomain, element, feature, values));" + NL + "\t\t\t\t\t\tif (valueToSet != null) {" + NL + "\t\t\t\t\t\t\tresult.append(";
   protected final String TEXT_1103 = ".create(editingDomain, element, feature, valueToSet));" + NL + "\t\t\t\t\t\t}";
   protected final String TEXT_1104 = NL + "\t\t\t\t\t\tresult.append(";
   protected final String TEXT_1105 = ".create(editingDomain, element, feature, valueToSet == null ? ";
   protected final String TEXT_1106 = ".UNSET_VALUE : valueToSet));";
   protected final String TEXT_1107 = NL + "\t\t\t\t\t\treturn result;" + NL + "\t\t\t\t\t}";
   protected final String TEXT_1108 = NL + "\t\t\t\t});";
   protected final String TEXT_1109 = NL + "\t\t}" + NL;
   protected final String TEXT_1110 = NL;
   protected final String TEXT_1111 = "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate ";
   protected final String TEXT_1112 = " manager;" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tpublic void performRequest(";
   protected final String TEXT_1113 = " req) {" + NL + "\t\tif (";
   protected final String TEXT_1114 = ".REQ_DIRECT_EDIT == req.getType()) {" + NL + "\t\t\tperformDirectEdit();" + NL + "\t\t} else {" + NL + "\t\t\tsuper.performRequest(req);" + NL + "\t\t}" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected ";
   protected final String TEXT_1115 = " getManager() {" + NL + "\t\tif (manager == null) {" + NL + "\t\t\tmanager = new ";
   protected final String TEXT_1116 = "(this, ";
   protected final String TEXT_1117 = ".class, new ";
   protected final String TEXT_1118 = "() {" + NL + "\t\t\t\tpublic void relocate(";
   protected final String TEXT_1119 = " celleditor) {" + NL + "\t\t\t\t\tif (checkTreeItem()) {" + NL + "\t\t\t\t\t\tcelleditor.getControl().setFont(((";
   protected final String TEXT_1120 = ") getWidget()).getFont());" + NL + "\t\t\t\t\t\tcelleditor.getControl().setBounds(((";
   protected final String TEXT_1121 = ") getWidget()).getBounds());" + NL + "\t\t\t\t\t}" + NL + "\t\t\t\t}" + NL + "\t\t\t}) {" + NL + "\t\t\t\tprotected void initCellEditor() {" + NL + "\t\t\t\t\tgetCellEditor().setValue(getEditText());" + NL + "\t\t\t\t}" + NL + "\t\t\t};" + NL + "\t\t}" + NL + "\t\treturn manager;" + NL + "\t}" + NL + "" + NL + "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprotected void performDirectEdit() {" + NL + "\t\tgetManager().show();" + NL + "\t}" + NL + "" + NL + "" + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprotected String getEditText() {" + NL + "\t\t\t";
   protected final String TEXT_1122 = " primaryLabelEditPart = getPrimaryLabelEditPart();" + NL + "\t\t\tif (primaryLabelEditPart != null) {" + NL + "\t\t\t\treturn primaryLabelEditPart.getLabelEditText();" + NL + "\t\t\t}" + NL + "\t\t\treturn \"\";" + NL + "\t\t}" + NL;
   protected final String TEXT_1123 = NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tpublic void activate() {" + NL + "\t\t\tsuper.activate();" + NL + "\t\t\tgetDiagramNode().getElement().eAdapters().add(domainModelRefresher);" + NL + "\t\t}" + NL + "" + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tpublic void deactivate() {" + NL + "\t\t\tgetDiagramNode().getElement().eAdapters().remove(domainModelRefresher);" + NL + "\t\t\tsuper.deactivate();" + NL + "\t\t}" + NL;
   protected final String TEXT_1124 = NL;
   protected final String TEXT_1125 = "\t/**" + NL + "\t * @generated" + NL + "\t */" + NL + "\tprivate ";
   protected final String TEXT_1126 = " domainModelRefresher = new ";
   protected final String TEXT_1127 = "(this);" + NL;
   protected final String TEXT_1128 = NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprotected String getText() {" + NL + "\t\t\t";
   protected final String TEXT_1129 = " primaryLabelEditPart = getPrimaryLabelEditPart();" + NL + "\t\t\tif (primaryLabelEditPart != null) {" + NL + "\t\t\t\treturn primaryLabelEditPart.getLabelText();" + NL + "\t\t\t}" + NL + "\t\t\treturn super.getText();" + NL + "\t\t}" + NL + "" + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprivate ";
   protected final String TEXT_1130 = " getPrimaryLabelEditPart() {" + NL + "\t\t\tfor(";
   protected final String TEXT_1131 = " it = getDiagramNode().getChildren().iterator(); it.hasNext(); ) {" + NL + "\t\t\t\t";
   protected final String TEXT_1132 = " nextChild = (";
   protected final String TEXT_1133 = ")it.next();" + NL + "\t\t\t\tif (";
   protected final String TEXT_1134 = ".getVisualID(nextChild) == ";
   protected final String TEXT_1135 = ".VISUAL_ID) {" + NL + "\t\t\t\t\treturn (";
   protected final String TEXT_1136 = ") ";
   protected final String TEXT_1137 = ".this.getViewer().getEditPartRegistry().get(nextChild);" + NL + "\t\t\t\t}" + NL + "\t\t\t}" + NL + "\t\t\treturn null;" + NL + "\t\t}";
   protected final String TEXT_1138 = NL + NL + "\t\t/**" + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprotected void createRefreshers() {" + NL + "\t\t\tsuper.createRefreshers();" + NL + "\t\t\tRefresher labelRefresher = new Refresher() {" + NL + "\t\t\t\tpublic void refresh() {" + NL + "\t\t\t\t\trefreshVisuals();" + NL + "\t\t\t\t}" + NL + "\t\t\t};";
   protected final String TEXT_1139 = NL + "\t\t\tregisterRefresher(";
   protected final String TEXT_1140 = ".eINSTANCE.get";
   protected final String TEXT_1141 = "(), labelRefresher);";
   protected final String TEXT_1142 = NL + "\t\t\tregisterRefresher(";
   protected final String TEXT_1143 = ".eINSTANCE.get";
   protected final String TEXT_1144 = "(), labelRefresher);";
   protected final String TEXT_1145 = NL + "\t\t}" + NL + "" + NL + "\t\t/**" + NL + "\t\t * Since compartments and labels are not selectable edit parts, they are filtered from the overview as well." + NL + "\t\t * @generated" + NL + "\t\t */" + NL + "\t\tprotected ";
   protected final String TEXT_1146 = " getModelChildren() {" + NL + "\t\t\t";
   protected final String TEXT_1147 = " result = new ";
   protected final String TEXT_1148 = "();" + NL + "\t\t\tfor(";
   protected final String TEXT_1149 = " it = getDiagramNode().getVisibleChildren().iterator(); it.hasNext(); ) {" + NL + "\t\t\t\t";
   protected final String TEXT_1150 = " next = (";
   protected final String TEXT_1151 = ") it.next();" + NL + "\t\t\t\t";
   protected final String TEXT_1152 = " style = (";
   protected final String TEXT_1153 = ") next.getStyle(";
   protected final String TEXT_1154 = ".eINSTANCE.getDrawerStyle());" + NL + "\t\t\t\tif (style != null && style.isCollapsed()) {" + NL + "\t\t\t\t\tcontinue;" + NL + "\t\t\t\t}" + NL + "\t\t\t\tswitch (";
   protected final String TEXT_1155 = ".getVisualID(next)) {";
   protected final String TEXT_1156 = NL + "\t\t\t\tcase ";
   protected final String TEXT_1157 = ".VISUAL_ID:" + NL + "\t\t\t\t\tresult.add(next);" + NL + "\t\t\t\t\tbreak;";
   protected final String TEXT_1158 = NL + "\t\t\t\tcase ";
   protected final String TEXT_1159 = ".VISUAL_ID:" + NL + "\t\t\t\t\tresult.addAll(next.getChildren());" + NL + "\t\t\t\t\tbreak;";
   protected final String TEXT_1160 = NL + "\t\t\t\t}" + NL + "\t\t\t}" + NL + "\t\t\tresult.addAll(getDiagramNode().getSourceEdges());" + NL + "\t\t\treturn result;" + NL + "\t\t}" + NL + "\t}" + NL + "}";
   protected final String TEXT_1161 = NL;
 
   public String generate(Object argument)
   {
     final StringBuffer stringBuffer = new StringBuffer();
     
 final GenCommonBase genElement = (GenCommonBase) ((Object[]) argument)[0];
 final GenNode genNode = (GenNode) genElement;
 final GenDiagram genDiagram = genNode.getDiagram();
 Palette palette = genDiagram.getPalette();
 boolean isXYLayout = ViewmapLayoutTypeHelper.getSharedInstance().isStoringChildPositions(genNode);
 final ImportAssistant importManager = (ImportAssistant) ((Object[]) argument)[1];
 importManager.registerInnerClass("LinkNotationModelRefresher");	//from linkNotationModelRefresher.jetinc
 importManager.registerInnerClass("NotationModelRefresher");	//from notationModelRefresher.jetinc
 importManager.registerInnerClass("TreeEditPartAdapter");
 
     stringBuffer.append(TEXT_1);
     stringBuffer.append(TEXT_2);
     
 class FeatureGetAccessorHelper {
 	/**
 	 * @param containerName the name of the container
 	 * @param feature the feature whose value is in interest
 	 * @param containerMetaClass the <code>GenClass</code> of the container, or <code>null</code>, if the container is declared as an <code>EObject</code>.
 	 * @param needsCastToResultType whether the cast to the result type is required (this parameter is only used if the <code>EClass</code> this feature belongs to is an external interface). 
 	 */
 	public void appendFeatureValueGetter(String containerName, GenFeature feature, GenClass containerMetaClass, boolean needsCastToResultType) {
 		if (feature.getGenClass().isExternalInterface()) {
 			boolean needsCastToEObject = containerMetaClass != null && containerMetaClass.isExternalInterface();
 			if (needsCastToResultType) {
 
     stringBuffer.append(TEXT_3);
     stringBuffer.append(importManager.getImportedName(feature.isListType() ? "java.util.Collection" : feature.getTypeGenClass().getQualifiedInterfaceName()));
     stringBuffer.append(TEXT_4);
     
 			}
 			if (needsCastToEObject) {
 
     stringBuffer.append(TEXT_5);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_6);
     
 			}
 
     stringBuffer.append(containerName);
     
 			if (needsCastToEObject) {
 
     stringBuffer.append(TEXT_7);
     
 			}
 
     stringBuffer.append(TEXT_8);
     stringBuffer.append(importManager.getImportedName(feature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_9);
     stringBuffer.append(feature.getFeatureAccessorName());
     stringBuffer.append(TEXT_10);
     
 			if (needsCastToResultType) {
 
     stringBuffer.append(TEXT_11);
     
 			}
 		} else {
 			boolean needsCastToFeatureGenType = containerMetaClass == null || containerMetaClass.isExternalInterface();
 			if (needsCastToFeatureGenType) {
 
     stringBuffer.append(TEXT_12);
     stringBuffer.append(importManager.getImportedName(feature.getGenClass().getQualifiedInterfaceName()));
     stringBuffer.append(TEXT_13);
     
 			}
 
     stringBuffer.append(containerName);
     
 			if (needsCastToFeatureGenType) {
 
     stringBuffer.append(TEXT_14);
     
 			}
 
     stringBuffer.append(TEXT_15);
     stringBuffer.append(feature.getGetAccessor());
     stringBuffer.append(TEXT_16);
     
 		}
 	}
 }
 final FeatureGetAccessorHelper myFeatureGetAccessorHelper = new FeatureGetAccessorHelper();
 
     stringBuffer.append(TEXT_17);
     
 class RelatedNodesFinder {
 	private HashMap<GenClass, Collection<GenNode>> myCache = new HashMap<GenClass, Collection<GenNode>>();
 	private final GenDiagram myDiagram;
 
 	public RelatedNodesFinder(GenDiagram genDiagram) {
 		myDiagram = genDiagram;
 	}
 
 	public Collection<GenNode> getRelatedGenNodes(GenClass genClass) {
 		Collection<GenNode> result = myCache.get(genClass);
 		if (result == null) {
 			result = new LinkedList<GenNode>();
 			myCache.put(genClass, result);
 			for(Iterator it = myDiagram.getAllNodes().iterator(); it.hasNext(); ) {
 				GenNode next = (GenNode) it.next();
 				if (genClass.equals(next.getDomainMetaClass())) {
 					result.add(next);
 				}
 			}
 		}
 		return result;
 	}
 }
 RelatedNodesFinder myRelatedNodesFinder = new RelatedNodesFinder(genDiagram);
 
     stringBuffer.append(TEXT_18);
     
 class NodeEditPartHelper {
 	private final List myInnerLabels = new LinkedList();
 	private final List myAllLabels = new LinkedList();
 	private final List myExternalLabels = new LinkedList();
 	private final List myPinnedCompartments = new LinkedList();
 	private final List myFloatingCompartments = new LinkedList();
 	private final List myContainedFeatureModelFacetLinks = new LinkedList();
 	private final List myContainedTypeModelFacetLinks = new LinkedList();
 	private GenNodeLabel myPrimaryLabel;
 	private boolean myHasChildrenInListCompartments = false;
 	private boolean hasIncomingLinks = false;
 	private boolean hasOutgoingLinks = false;
 
 	public NodeEditPartHelper(GenNode genNode){
 		myPrimaryLabel = null;
 
 		for (Iterator labels = genNode.getLabels().iterator(); labels.hasNext();) {
 			GenNodeLabel next = (GenNodeLabel) labels.next();
 			if (myPrimaryLabel == null && !next.isReadOnly()){
 				myPrimaryLabel = next;
 			}
 			myAllLabels.add(next);
 			if (next instanceof GenExternalNodeLabel) {
 				myExternalLabels.add(next);
 			} else {
 				if (next.getViewmap() instanceof ParentAssignedViewmap) {
 					myInnerLabels.add(next);
 				}
 			}
 		}
 		
 		for (Iterator compartments = genNode.getCompartments().iterator(); compartments.hasNext();){
 			GenCompartment next = (GenCompartment) compartments.next();
 			if (next.getViewmap() instanceof ParentAssignedViewmap){
 				myPinnedCompartments.add(next);
 			} else {
 				myFloatingCompartments.add(next);
 			}	
 			
 			myHasChildrenInListCompartments |= next.isListLayout() && !next.getChildNodes().isEmpty();
 		}
 
 		for(Iterator it = genDiagram.getLinks().iterator(); it.hasNext(); ) {
 			GenLink genLink = (GenLink)it.next();
 			if (!genLink.isViewDirectionAlignedWithModel() || genLink.getModelFacet() == null) {
 				continue;
 			}
 			GenClass incomingClass;
 			GenClass outgoingClass;
 			GenClass containerClass;
 			if (genLink.getModelFacet() instanceof TypeLinkModelFacet) {
 				TypeLinkModelFacet modelFacet = (TypeLinkModelFacet) genLink.getModelFacet();
 				incomingClass = modelFacet.getTargetMetaFeature().getTypeGenClass();
 				outgoingClass = modelFacet.getSourceMetaFeature() == null
 					? modelFacet.getContainmentMetaFeature().getGenClass()
 					: modelFacet.getSourceMetaFeature().getTypeGenClass();
 				if (modelFacet.getSourceMetaFeature() == null && modelFacet.getTargetMetaFeature() == null) {
 					//if one link feature is null, the element is treated as this end of the link. If both are null, we cannot do anything about such a link.
 					containerClass = null;
 				} else {
 					containerClass = modelFacet.getContainmentMetaFeature().getGenClass();
 				}
 			} else if (genLink.getModelFacet() instanceof FeatureLinkModelFacet) {
 				GenFeature metaFeature = ((FeatureLinkModelFacet) genLink.getModelFacet()).getMetaFeature();
 				incomingClass = metaFeature.getTypeGenClass();
 				outgoingClass = metaFeature.getGenClass();
 				containerClass = metaFeature.getGenClass();
 			} else {
 				continue;
 			}
 			hasIncomingLinks |= (incomingClass.getEcoreClass().isSuperTypeOf(genNode.getModelFacet().getMetaClass().getEcoreClass()));
 			hasOutgoingLinks |= (outgoingClass.getEcoreClass().isSuperTypeOf(genNode.getModelFacet().getMetaClass().getEcoreClass()));
 			if (containerClass != null && containerClass.getEcoreClass().isSuperTypeOf(genNode.getModelFacet().getMetaClass().getEcoreClass())) {
 				if (genLink.getModelFacet() instanceof TypeLinkModelFacet) {
 					myContainedTypeModelFacetLinks.add(genLink);
 				} else {
 					myContainedFeatureModelFacetLinks.add(genLink);
 				}
 			}
 		}
 	}
 
 	public boolean hasBothChildrenAndCompartments() {
 		return !genNode.getCompartments().isEmpty() && !genNode.getChildNodes().isEmpty();
 	}
 
 	public boolean containsLinks() {
 		return containsFeatureModelFacetLinks() || containsTypeModelFacetLinks();
 	}
 
 	public boolean containsFeatureModelFacetLinks() {
 		return !myContainedFeatureModelFacetLinks.isEmpty();
 	}
 
 	public boolean containsTypeModelFacetLinks() {
 		return !myContainedTypeModelFacetLinks.isEmpty();
 	}
 
 	public boolean hasIncomingLinks() {
 		return hasIncomingLinks;
 	}
 
 	public boolean hasOutgoingLinks() {
 		return hasOutgoingLinks;
 	}
 
 	public boolean hasChildrenInListCompartments(){
 		return myHasChildrenInListCompartments;
 	}
 	
 	public boolean hasInnerFixedLabels(){
 		return !myInnerLabels.isEmpty();
 	}
 	
 	public boolean hasPinnedCompartments(){
 		return !myPinnedCompartments.isEmpty();
 	}
 	
 	public boolean hasFixedChildren(){
 		return hasInnerFixedLabels() || hasPinnedCompartments();
 	}
 	
 	public boolean hasExternalLabels(){
 		return !myExternalLabels.isEmpty();
 	}
 	
 	public GenNodeLabel getPrimaryLabel(){
 		return myPrimaryLabel;
 	}
 	
 	public Iterator getInnerFixedLabels(){
 		return myInnerLabels.iterator();
 	}
 	
 	public Iterator getExternalLabels(){
 		return myExternalLabels.iterator();
 	}
 	
 	public Iterator getPinnedCompartments(){
 		return myPinnedCompartments.iterator();
 	}
 
 	public Iterator getAllLabels() {
 		return myAllLabels.iterator();
 	}
 
 	public Iterator getContainedFeatureModelFacetLinks() {
 		return myContainedFeatureModelFacetLinks.iterator();
 	}
 
 	public Iterator getContainedTypeModelFacetLinks() {
 		return myContainedTypeModelFacetLinks.iterator();
 	}
 }
 final NodeEditPartHelper myHelper = new NodeEditPartHelper(genNode);
 
     stringBuffer.append(TEXT_19);
     
 String copyrightText = genDiagram.getEditorGen().getCopyrightText();
 if (copyrightText != null && copyrightText.trim().length() > 0) {
 
     stringBuffer.append(TEXT_20);
     stringBuffer.append(copyrightText.replaceAll("\n", "\n * "));
     stringBuffer.append(TEXT_21);
     }
     stringBuffer.append(TEXT_22);
     importManager.emitPackageStatement(stringBuffer);
     
 importManager.markImportLocation(stringBuffer);
 
     stringBuffer.append(TEXT_23);
     stringBuffer.append(genNode.getEditPartClassName());
     stringBuffer.append(TEXT_24);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.editparts.AbstractGraphicalEditPart"));
     stringBuffer.append(TEXT_25);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.NodeEditPart"));
     stringBuffer.append(TEXT_26);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.IUpdatableEditPart"));
     stringBuffer.append(TEXT_27);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.decorations.IDecoratableEditPart"));
     stringBuffer.append(TEXT_28);
     {
 GenCommonBase genCommonBase = genNode;
     stringBuffer.append(TEXT_29);
     stringBuffer.append(TEXT_30);
     stringBuffer.append(genCommonBase.getVisualID());
     stringBuffer.append(TEXT_31);
     }
     stringBuffer.append(TEXT_32);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_33);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_34);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.decorations.IDecorationManager"));
     stringBuffer.append(TEXT_35);
     
 	if (myHelper.hasBothChildrenAndCompartments()) {
 
     stringBuffer.append(TEXT_36);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_37);
     
 	}
 
     stringBuffer.append(TEXT_38);
     stringBuffer.append(genNode.getEditPartClassName());
     stringBuffer.append(TEXT_39);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_40);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_41);
     stringBuffer.append(TEXT_42);
     stringBuffer.append(TEXT_43);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_44);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_45);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_46);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_47);
     stringBuffer.append(TEXT_48);
     stringBuffer.append(TEXT_49);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPolicy"));
     stringBuffer.append(TEXT_50);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.editpolicies.ComponentEditPolicy"));
     stringBuffer.append(TEXT_51);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.Command"));
     stringBuffer.append(TEXT_52);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.requests.GroupRequest"));
     stringBuffer.append(TEXT_53);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.TransactionalEditingDomain"));
     stringBuffer.append(TEXT_54);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.util.TransactionUtil"));
     stringBuffer.append(TEXT_55);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_56);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_57);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.RemoveNotationalElementCommand"));
     stringBuffer.append(TEXT_58);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_59);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.WrappingCommand"));
     stringBuffer.append(TEXT_60);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.TransactionalEditingDomain"));
     stringBuffer.append(TEXT_61);
     
 {
 TypeModelFacet facet = genNode.getModelFacet();
 GenFeature childFeature = facet.getChildMetaFeature();
 GenFeature containmentFeature = facet.getContainmentMetaFeature();
 if (childFeature != null && childFeature != containmentFeature && !childFeature.isDerived()) {
 
     stringBuffer.append(TEXT_62);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_63);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_64);
     
 	if (containmentFeature.getEcoreFeature().isMany()) {
 
     stringBuffer.append(TEXT_65);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.RemoveCommand"));
     stringBuffer.append(TEXT_66);
     stringBuffer.append(importManager.getImportedName(containmentFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_67);
     stringBuffer.append(containmentFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_68);
     
 	} else {
 
     stringBuffer.append(TEXT_69);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_70);
     stringBuffer.append(importManager.getImportedName(containmentFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_71);
     stringBuffer.append(containmentFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_72);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_73);
     
 	}
 
     
 	if (childFeature.getEcoreFeature().isMany()) {
 
     stringBuffer.append(TEXT_74);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.RemoveCommand"));
     stringBuffer.append(TEXT_75);
     stringBuffer.append(importManager.getImportedName(childFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_76);
     stringBuffer.append(childFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_77);
     
 	} else {
 
     stringBuffer.append(TEXT_78);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_79);
     stringBuffer.append(importManager.getImportedName(childFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_80);
     stringBuffer.append(childFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_81);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_82);
     
 	}
 
     stringBuffer.append(TEXT_83);
     
 } else {
 	if (containmentFeature.getEcoreFeature().isMany()) {
 
     stringBuffer.append(TEXT_84);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.RemoveCommand"));
     stringBuffer.append(TEXT_85);
     stringBuffer.append(importManager.getImportedName(containmentFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_86);
     stringBuffer.append(containmentFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_87);
     
 	} else {
 
     stringBuffer.append(TEXT_88);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_89);
     stringBuffer.append(importManager.getImportedName(containmentFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_90);
     stringBuffer.append(containmentFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_91);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_92);
     
 	}
 }
 
     stringBuffer.append(TEXT_93);
     
 }	/*restrict local vars used in component edit policy*/
 
     
 String layoutEditPolicyBaseClass;
 if (!genNode.getChildNodes().isEmpty() && isXYLayout) {
 	layoutEditPolicyBaseClass = "org.eclipse.gef.editpolicies.XYLayoutEditPolicy";
 } else {
 	layoutEditPolicyBaseClass = "org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy";
 }
 
     stringBuffer.append(TEXT_94);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPolicy"));
     stringBuffer.append(TEXT_95);
     stringBuffer.append(importManager.getImportedName(layoutEditPolicyBaseClass));
     stringBuffer.append(TEXT_96);
     
 {
 	String _getViewCode = "getDiagramNode()";
 	List childNodes = genNode.getChildNodes();
 	boolean isListLayout = !isXYLayout;
 
     stringBuffer.append(TEXT_97);
     
 if (!childNodes.isEmpty()) {
 
     stringBuffer.append(TEXT_98);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_99);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_100);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_101);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_102);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.TransactionalEditingDomain"));
     stringBuffer.append(TEXT_103);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.util.TransactionUtil"));
     stringBuffer.append(TEXT_104);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_105);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_106);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_107);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_108);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_109);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.Command"));
     stringBuffer.append(TEXT_110);
     
 	for(Iterator it = childNodes.iterator(); it.hasNext(); ) {
 		GenNode next = (GenNode) it.next();
 
     stringBuffer.append(TEXT_111);
     stringBuffer.append(importManager.getImportedName(next.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_112);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_113);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_114);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_115);
     
 		{
 			Collection<GenNode> relatedNodes = myRelatedNodesFinder.getRelatedGenNodes(next.getDomainMetaClass());
 			if (relatedNodes.size() != 1) {
 
     stringBuffer.append(TEXT_116);
     
 			}
 			for(GenNode nextRelated : relatedNodes) {
 				TypeModelFacet facet = nextRelated.getModelFacet();
 				GenFeature childFeature = facet.getChildMetaFeature();
 				GenFeature containmentFeature = facet.getContainmentMetaFeature();
 				if (relatedNodes.size() != 1) {
 
     stringBuffer.append(TEXT_117);
     stringBuffer.append(importManager.getImportedName(nextRelated.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_118);
     
 				}
 				if (childFeature != null && childFeature != containmentFeature && !childFeature.isDerived()) {
 					if (childFeature.getEcoreFeature().isMany()) {
 
     stringBuffer.append(TEXT_119);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.RemoveCommand"));
     stringBuffer.append(TEXT_120);
     stringBuffer.append(importManager.getImportedName(childFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_121);
     stringBuffer.append(childFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_122);
     
 					} else {
 
     stringBuffer.append(TEXT_123);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_124);
     stringBuffer.append(importManager.getImportedName(childFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_125);
     stringBuffer.append(childFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_126);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_127);
     
 					}
 				}
 				if (containmentFeature.getEcoreFeature().isMany()) {
 
     stringBuffer.append(TEXT_128);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.RemoveCommand"));
     stringBuffer.append(TEXT_129);
     stringBuffer.append(importManager.getImportedName(containmentFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_130);
     stringBuffer.append(containmentFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_131);
     
 				} else {
 
     stringBuffer.append(TEXT_132);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_133);
     stringBuffer.append(importManager.getImportedName(containmentFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_134);
     stringBuffer.append(containmentFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_135);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_136);
     
 				}
 				if (relatedNodes.size() != 1) {
 
     stringBuffer.append(TEXT_137);
     
 				}
 			}	//for(GenNode nextRelated : relatedNodes)
 			if (relatedNodes.size() != 1) {
 
     stringBuffer.append(TEXT_138);
     /*switch(visualID)*/
     
 			}
 			TypeModelFacet facet = next.getModelFacet();
 			GenFeature childFeature = facet.getChildMetaFeature();
 			GenFeature containmentFeature = facet.getContainmentMetaFeature();
 			if (childFeature != null && childFeature != containmentFeature && !childFeature.isDerived()) {
 				GenFeature _feature = childFeature;
 				String _ownerInstance = _getViewCode + ".getElement()";
 				String _exceedsUpperBound = "return " + importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand") + ".INSTANCE;";
 				GenClass _ownerGenClass = null;
 
     stringBuffer.append(TEXT_139);
     
 int upperBound = _feature.getEcoreFeature().getUpperBound();
 if (upperBound == 1) {
 
     stringBuffer.append(TEXT_140);
     myFeatureGetAccessorHelper.appendFeatureValueGetter(_ownerInstance, _feature, _ownerGenClass, false);
     stringBuffer.append(TEXT_141);
     stringBuffer.append(_exceedsUpperBound);
     stringBuffer.append(TEXT_142);
     
 } else {
 	if (upperBound > 0) {
 
     stringBuffer.append(TEXT_143);
     myFeatureGetAccessorHelper.appendFeatureValueGetter(_ownerInstance, _feature, _ownerGenClass, true);
     stringBuffer.append(TEXT_144);
     stringBuffer.append(importManager.getImportedName(_feature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_145);
     stringBuffer.append(_feature.getFeatureAccessorName());
     stringBuffer.append(TEXT_146);
     stringBuffer.append(_exceedsUpperBound);
     stringBuffer.append(TEXT_147);
     
 	}
 }
 
     stringBuffer.append(TEXT_148);
     stringBuffer.append(importManager.getImportedName(childFeature.getEcoreFeature().isMany() ? "org.eclipse.emf.edit.command.AddCommand" : "org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_149);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_150);
     stringBuffer.append(importManager.getImportedName(childFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_151);
     stringBuffer.append(childFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_152);
     
 			}
 			{
 				GenFeature _feature = containmentFeature;
 				String _ownerInstance = _getViewCode + ".getElement()";
 				String _exceedsUpperBound = "return " + importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand") + ".INSTANCE;";
 				GenClass _ownerGenClass = null;
 
     stringBuffer.append(TEXT_153);
     
 int upperBound = _feature.getEcoreFeature().getUpperBound();
 if (upperBound == 1) {
 
     stringBuffer.append(TEXT_154);
     myFeatureGetAccessorHelper.appendFeatureValueGetter(_ownerInstance, _feature, _ownerGenClass, false);
     stringBuffer.append(TEXT_155);
     stringBuffer.append(_exceedsUpperBound);
     stringBuffer.append(TEXT_156);
     
 } else {
 	if (upperBound > 0) {
 
     stringBuffer.append(TEXT_157);
     myFeatureGetAccessorHelper.appendFeatureValueGetter(_ownerInstance, _feature, _ownerGenClass, true);
     stringBuffer.append(TEXT_158);
     stringBuffer.append(importManager.getImportedName(_feature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_159);
     stringBuffer.append(_feature.getFeatureAccessorName());
     stringBuffer.append(TEXT_160);
     stringBuffer.append(_exceedsUpperBound);
     stringBuffer.append(TEXT_161);
     
 	}
 }
 
     
 			}
 
     stringBuffer.append(TEXT_162);
     stringBuffer.append(importManager.getImportedName(containmentFeature.getEcoreFeature().isMany() ? "org.eclipse.emf.edit.command.AddCommand" : "org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_163);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_164);
     stringBuffer.append(importManager.getImportedName(containmentFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_165);
     stringBuffer.append(containmentFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_166);
     
 		}	//local declaration for related nodes.
 
     stringBuffer.append(TEXT_167);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.RemoveCommand"));
     stringBuffer.append(TEXT_168);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.ExpandDrawerCommand"));
     stringBuffer.append(TEXT_169);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_170);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.AddCommand"));
     stringBuffer.append(TEXT_171);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_172);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_173);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.SetBoundsCommand"));
     stringBuffer.append(TEXT_174);
     if (isListLayout) {
     stringBuffer.append(TEXT_175);
     } else {
     stringBuffer.append(TEXT_176);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.geometry.Rectangle"));
     stringBuffer.append(TEXT_177);
     }
     stringBuffer.append(TEXT_178);
     
 		{
 			int defaultWidth = 40;
 			int defaultHeight = 40;
 			DefaultSizeAttributes defSizeAttrs = (DefaultSizeAttributes) next.getViewmap().find(DefaultSizeAttributes.class);
 			if (defSizeAttrs != null) {
 				defaultWidth = defSizeAttrs.getWidth();
 				defaultHeight = defSizeAttrs.getHeight();
 			}
 
     stringBuffer.append(TEXT_179);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.CreateNotationalNodeCommand"));
     stringBuffer.append(TEXT_180);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_181);
     if (isListLayout) {
     stringBuffer.append(TEXT_182);
     } else {
     stringBuffer.append(TEXT_183);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.geometry.Rectangle"));
     stringBuffer.append(TEXT_184);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.geometry.Dimension"));
     stringBuffer.append(TEXT_185);
     stringBuffer.append(defaultWidth);
     stringBuffer.append(TEXT_186);
     stringBuffer.append(defaultHeight);
     stringBuffer.append(TEXT_187);
     }
     stringBuffer.append(TEXT_188);
     stringBuffer.append(importManager.getImportedName(next.getNotationViewFactoryQualifiedClassName()));
     stringBuffer.append(TEXT_189);
     
 		}
 
     stringBuffer.append(TEXT_190);
     
 	}	//for
 
     stringBuffer.append(TEXT_191);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.WrappingCommand"));
     stringBuffer.append(TEXT_192);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_193);
     
 }	//if (!childNodes.isEmpty()) in createAddCommand
 
     stringBuffer.append(TEXT_194);
     
 /* delegation from getCloneCommand to createCloneCommand is 100% analogous to delegation from getAddCommand() to createAddCommand() in ConstrainedLayoutEditPolicy. */
 
     
 if (!childNodes.isEmpty()) {
 
     stringBuffer.append(TEXT_195);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.Command"));
     stringBuffer.append(TEXT_196);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.requests.ChangeBoundsRequest"));
     stringBuffer.append(TEXT_197);
     stringBuffer.append(importManager.getImportedName("java.util.List"));
     stringBuffer.append(TEXT_198);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.CompoundCommand"));
     stringBuffer.append(TEXT_199);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.CompoundCommand"));
     stringBuffer.append(TEXT_200);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.GraphicalEditPart"));
     stringBuffer.append(TEXT_201);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.geometry.Rectangle"));
     stringBuffer.append(TEXT_202);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.GraphicalEditPart"));
     stringBuffer.append(TEXT_203);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.Command"));
     stringBuffer.append(TEXT_204);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_205);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_206);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_207);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_208);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.TransactionalEditingDomain"));
     stringBuffer.append(TEXT_209);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.util.TransactionUtil"));
     stringBuffer.append(TEXT_210);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_211);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_212);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_213);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_214);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.Command"));
     stringBuffer.append(TEXT_215);
     
 	for(Iterator it = childNodes.iterator(); it.hasNext(); ) {
 		GenNode next = (GenNode) it.next();
 		TypeModelFacet facet = next.getModelFacet();
 		GenFeature childFeature = facet.getChildMetaFeature();
 		GenFeature containmentFeature = facet.getContainmentMetaFeature();
 
     stringBuffer.append(TEXT_216);
     stringBuffer.append(importManager.getImportedName(next.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_217);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CommandWrapper"));
     stringBuffer.append(TEXT_218);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_219);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.Command"));
     stringBuffer.append(TEXT_220);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_221);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.CopyCommand"));
     stringBuffer.append(TEXT_222);
     
 		if (childFeature != null && childFeature != containmentFeature && !childFeature.isDerived()) {
 			GenFeature _feature = childFeature;
 			String _ownerInstance = _getViewCode + ".getElement()";
 			String _exceedsUpperBound = "return false;";
 			GenClass _ownerGenClass = null;
 
     stringBuffer.append(TEXT_223);
     
 int upperBound = _feature.getEcoreFeature().getUpperBound();
 if (upperBound == 1) {
 
     stringBuffer.append(TEXT_224);
     myFeatureGetAccessorHelper.appendFeatureValueGetter(_ownerInstance, _feature, _ownerGenClass, false);
     stringBuffer.append(TEXT_225);
     stringBuffer.append(_exceedsUpperBound);
     stringBuffer.append(TEXT_226);
     
 } else {
 	if (upperBound > 0) {
 
     stringBuffer.append(TEXT_227);
     myFeatureGetAccessorHelper.appendFeatureValueGetter(_ownerInstance, _feature, _ownerGenClass, true);
     stringBuffer.append(TEXT_228);
     stringBuffer.append(importManager.getImportedName(_feature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_229);
     stringBuffer.append(_feature.getFeatureAccessorName());
     stringBuffer.append(TEXT_230);
     stringBuffer.append(_exceedsUpperBound);
     stringBuffer.append(TEXT_231);
     
 	}
 }
 
     
 		}
 		{
 			GenFeature _feature = containmentFeature;
 			String _ownerInstance = _getViewCode + ".getElement()";
 			String _exceedsUpperBound = "return false;";
 			GenClass _ownerGenClass = null;
 
     stringBuffer.append(TEXT_232);
     
 int upperBound = _feature.getEcoreFeature().getUpperBound();
 if (upperBound == 1) {
 
     stringBuffer.append(TEXT_233);
     myFeatureGetAccessorHelper.appendFeatureValueGetter(_ownerInstance, _feature, _ownerGenClass, false);
     stringBuffer.append(TEXT_234);
     stringBuffer.append(_exceedsUpperBound);
     stringBuffer.append(TEXT_235);
     
 } else {
 	if (upperBound > 0) {
 
     stringBuffer.append(TEXT_236);
     myFeatureGetAccessorHelper.appendFeatureValueGetter(_ownerInstance, _feature, _ownerGenClass, true);
     stringBuffer.append(TEXT_237);
     stringBuffer.append(importManager.getImportedName(_feature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_238);
     stringBuffer.append(_feature.getFeatureAccessorName());
     stringBuffer.append(TEXT_239);
     stringBuffer.append(_exceedsUpperBound);
     stringBuffer.append(TEXT_240);
     
 	}
 }
 
     
 		}
 
     stringBuffer.append(TEXT_241);
     stringBuffer.append(importManager.getImportedName("java.util.Collection"));
     stringBuffer.append(TEXT_242);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_243);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_244);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_245);
     
 		if (childFeature != null && childFeature != containmentFeature && !childFeature.isDerived()) {
 
     stringBuffer.append(TEXT_246);
     stringBuffer.append(importManager.getImportedName(childFeature.getEcoreFeature().isMany() ? "org.eclipse.emf.edit.command.AddCommand" : "org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_247);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_248);
     stringBuffer.append(importManager.getImportedName(childFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_249);
     stringBuffer.append(childFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_250);
     
 		}
 
     stringBuffer.append(TEXT_251);
     stringBuffer.append(importManager.getImportedName(containmentFeature.getEcoreFeature().isMany() ? "org.eclipse.emf.edit.command.AddCommand" : "org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_252);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_253);
     stringBuffer.append(importManager.getImportedName(containmentFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_254);
     stringBuffer.append(containmentFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_255);
     
 		{
 			int defaultWidth = 40;
 			int defaultHeight = 40;
 			DefaultSizeAttributes defSizeAttrs = (DefaultSizeAttributes) next.getViewmap().find(DefaultSizeAttributes.class);
 			if (defSizeAttrs != null) {
 				defaultWidth = defSizeAttrs.getWidth();
 				defaultHeight = defSizeAttrs.getHeight();
 			}
 
     stringBuffer.append(TEXT_256);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.CreateNotationalNodeCommand"));
     stringBuffer.append(TEXT_257);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_258);
     if (isListLayout) {
     stringBuffer.append(TEXT_259);
     } else {
     stringBuffer.append(TEXT_260);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.geometry.Rectangle"));
     stringBuffer.append(TEXT_261);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.geometry.Dimension"));
     stringBuffer.append(TEXT_262);
     stringBuffer.append(defaultWidth);
     stringBuffer.append(TEXT_263);
     stringBuffer.append(defaultHeight);
     stringBuffer.append(TEXT_264);
     }
     stringBuffer.append(TEXT_265);
     stringBuffer.append(importManager.getImportedName(next.getNotationViewFactoryQualifiedClassName()));
     stringBuffer.append(TEXT_266);
     
 		}
 	}	//for
 
     stringBuffer.append(TEXT_267);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.WrappingCommand"));
     stringBuffer.append(TEXT_268);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_269);
     
 }	//if (!childNodes.isEmpty())
 
     
 }
 
     stringBuffer.append(TEXT_270);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.Command"));
     stringBuffer.append(TEXT_271);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.requests.CreateRequest"));
     stringBuffer.append(TEXT_272);
     
 if (palette != null && !genNode.getChildNodes().isEmpty()) {
 
     stringBuffer.append(TEXT_273);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.requests.CreateRequestEx"));
     stringBuffer.append(TEXT_274);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.requests.CreateRequestEx"));
     stringBuffer.append(TEXT_275);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.requests.CreateRequestEx"));
     stringBuffer.append(TEXT_276);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_277);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_278);
     
 	for(Iterator it = genNode.getChildNodes().iterator(); it.hasNext(); ) {
 		GenChildNode next = (GenChildNode)it.next();
 
     stringBuffer.append(TEXT_279);
     stringBuffer.append(importManager.getImportedName(next.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_280);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditCommandsPackageName() + ".Create" + next.getDomainMetaClass().getName() + next.getVisualID() + "Command"));
     stringBuffer.append(TEXT_281);
     if (isXYLayout) {
     stringBuffer.append(TEXT_282);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.geometry.Rectangle"));
     stringBuffer.append(TEXT_283);
     }
     stringBuffer.append(TEXT_284);
     
 	}
 
     stringBuffer.append(TEXT_285);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.WrappingCommand"));
     stringBuffer.append(TEXT_286);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.util.TransactionUtil"));
     stringBuffer.append(TEXT_287);
     
 }
 
     stringBuffer.append(TEXT_288);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_289);
     
 if (!genNode.getChildNodes().isEmpty() && isXYLayout) {
 
     stringBuffer.append(TEXT_290);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.Command"));
     stringBuffer.append(TEXT_291);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.requests.ChangeBoundsRequest"));
     stringBuffer.append(TEXT_292);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_293);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_294);
     stringBuffer.append(TEXT_295);
     stringBuffer.append(TEXT_296);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_297);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_298);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.Command"));
     stringBuffer.append(TEXT_299);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.ChangeBoundsCommand"));
     stringBuffer.append(TEXT_300);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.GraphicalEditPart"));
     stringBuffer.append(TEXT_301);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.WrappingCommand"));
     stringBuffer.append(TEXT_302);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.util.TransactionUtil"));
     stringBuffer.append(TEXT_303);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_304);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_305);
     
 } else {
 
     stringBuffer.append(TEXT_306);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.geometry.Rectangle"));
     stringBuffer.append(TEXT_307);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.geometry.Point"));
     stringBuffer.append(TEXT_308);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.Command"));
     stringBuffer.append(TEXT_309);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_310);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_311);
     
 }
 
     stringBuffer.append(TEXT_312);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPolicy"));
     stringBuffer.append(TEXT_313);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_314);
     
 if (!genNode.getChildNodes().isEmpty()) {
 
     stringBuffer.append(TEXT_315);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPolicy"));
     stringBuffer.append(TEXT_316);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPolicy"));
     stringBuffer.append(TEXT_317);
     
 }
 
     stringBuffer.append(TEXT_318);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.editpolicies.ResizableEditPolicy"));
     stringBuffer.append(TEXT_319);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_320);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.Request"));
     stringBuffer.append(TEXT_321);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.RequestConstants"));
     stringBuffer.append(TEXT_322);
     stringBuffer.append(genNode.getEditPartClassName());
     stringBuffer.append(TEXT_323);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPolicy"));
     stringBuffer.append(TEXT_324);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy"));
     stringBuffer.append(TEXT_325);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.Command"));
     stringBuffer.append(TEXT_326);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.requests.ReconnectRequest"));
     stringBuffer.append(TEXT_327);
     
 if (!myHelper.hasIncomingLinks()) {
 
     stringBuffer.append(TEXT_328);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_329);
     
 } else {
 
     stringBuffer.append(TEXT_330);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.ConnectionEditPart"));
     stringBuffer.append(TEXT_331);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Edge"));
     stringBuffer.append(TEXT_332);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_333);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Edge"));
     stringBuffer.append(TEXT_334);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Edge"));
     stringBuffer.append(TEXT_335);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_336);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_337);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_338);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.Command"));
     stringBuffer.append(TEXT_339);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_340);
     
 	for(Iterator it = genDiagram.getLinks().iterator(); it.hasNext(); ) {
 		GenLink genLink = (GenLink)it.next();
 		if (!genLink.isViewDirectionAlignedWithModel() || genLink.getModelFacet() == null) {
 			continue;
 		}
 		GenClass incomingClass;
 		String reconnectCommandNameInfix;
 		if (genLink.getModelFacet() instanceof TypeLinkModelFacet) {
 			TypeLinkModelFacet modelFacet = (TypeLinkModelFacet) genLink.getModelFacet();
 			incomingClass = modelFacet.getTargetMetaFeature().getTypeGenClass();
 			reconnectCommandNameInfix = modelFacet.getMetaClass().getName();
 		} else if (genLink.getModelFacet() instanceof FeatureLinkModelFacet) {
 			GenFeature metaFeature = ((FeatureLinkModelFacet) genLink.getModelFacet()).getMetaFeature();
 			incomingClass = metaFeature.getTypeGenClass();
 			reconnectCommandNameInfix = metaFeature.getFeatureAccessorName();
 		} else {
 			continue;
 		}
 		if (!incomingClass.getEcoreClass().isSuperTypeOf(genNode.getModelFacet().getMetaClass().getEcoreClass())) {
 			continue;
 		}
 
     stringBuffer.append(TEXT_341);
     stringBuffer.append(importManager.getImportedName(genLink.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_342);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditCommandsPackageName() + ".Reconnect" + reconnectCommandNameInfix + genLink.getVisualID() + "TargetCommand"));
     stringBuffer.append(TEXT_343);
     
 	}
 
     stringBuffer.append(TEXT_344);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_345);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.WrappingCommand"));
     stringBuffer.append(TEXT_346);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.util.TransactionUtil"));
     stringBuffer.append(TEXT_347);
     
 }
 
     stringBuffer.append(TEXT_348);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.Command"));
     stringBuffer.append(TEXT_349);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.requests.ReconnectRequest"));
     stringBuffer.append(TEXT_350);
     
 if (!myHelper.hasOutgoingLinks()) {
 
     stringBuffer.append(TEXT_351);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_352);
     
 } else {
 
     stringBuffer.append(TEXT_353);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.ConnectionEditPart"));
     stringBuffer.append(TEXT_354);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Edge"));
     stringBuffer.append(TEXT_355);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_356);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Edge"));
     stringBuffer.append(TEXT_357);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Edge"));
     stringBuffer.append(TEXT_358);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_359);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_360);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_361);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.Command"));
     stringBuffer.append(TEXT_362);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_363);
     
 	for(Iterator it = genDiagram.getLinks().iterator(); it.hasNext(); ) {
 		GenLink genLink = (GenLink)it.next();
 		if (!genLink.isViewDirectionAlignedWithModel() || genLink.getModelFacet() == null) {
 			continue;
 		}
 		GenClass outgoingClass;
 		String reconnectCommandNameInfix;
 		if (genLink.getModelFacet() instanceof TypeLinkModelFacet) {
 			TypeLinkModelFacet modelFacet = (TypeLinkModelFacet) genLink.getModelFacet();
 			outgoingClass = modelFacet.getSourceMetaFeature() == null
 				? modelFacet.getContainmentMetaFeature().getGenClass()
 				: modelFacet.getSourceMetaFeature().getTypeGenClass();
 			reconnectCommandNameInfix = modelFacet.getMetaClass().getName();
 		} else if (genLink.getModelFacet() instanceof FeatureLinkModelFacet) {
 			GenFeature metaFeature = ((FeatureLinkModelFacet) genLink.getModelFacet()).getMetaFeature();
 			outgoingClass = metaFeature.getGenClass();
 			reconnectCommandNameInfix = metaFeature.getFeatureAccessorName();
 		} else {
 			continue;
 		}
 		if (!outgoingClass.getEcoreClass().isSuperTypeOf(genNode.getModelFacet().getMetaClass().getEcoreClass())) {
 			continue;
 		}
 
     stringBuffer.append(TEXT_364);
     stringBuffer.append(importManager.getImportedName(genLink.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_365);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditCommandsPackageName() + ".Reconnect" + reconnectCommandNameInfix + genLink.getVisualID() + "SourceCommand"));
     stringBuffer.append(TEXT_366);
     
 	}
 
     stringBuffer.append(TEXT_367);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_368);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.WrappingCommand"));
     stringBuffer.append(TEXT_369);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.util.TransactionUtil"));
     stringBuffer.append(TEXT_370);
     
 }
 
     stringBuffer.append(TEXT_371);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.Command"));
     stringBuffer.append(TEXT_372);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.requests.CreateConnectionRequest"));
     stringBuffer.append(TEXT_373);
     if (palette != null && myHelper.hasOutgoingLinks()) {
     stringBuffer.append(TEXT_374);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.requests.CreateConnectionRequestEx"));
     stringBuffer.append(TEXT_375);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.requests.CreateConnectionRequestEx"));
     stringBuffer.append(TEXT_376);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.requests.CreateConnectionRequestEx"));
     stringBuffer.append(TEXT_377);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_378);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_379);
     
 for(Iterator it = genDiagram.getLinks().iterator(); it.hasNext(); ) {
 	GenLink genLink = (GenLink)it.next();
 	if (!genLink.isViewDirectionAlignedWithModel() || genLink.getModelFacet() == null) {
 		continue;
 	}
 	GenClass outgoingClass;
 	String createCommandNameInfix;
 	if (genLink.getModelFacet() instanceof TypeLinkModelFacet) {
 		TypeLinkModelFacet modelFacet = (TypeLinkModelFacet) genLink.getModelFacet();
 		outgoingClass = modelFacet.getSourceMetaFeature() == null
 			? modelFacet.getContainmentMetaFeature().getGenClass()
 			: modelFacet.getSourceMetaFeature().getTypeGenClass();
 		createCommandNameInfix = modelFacet.getMetaClass().getName();
 	} else if (genLink.getModelFacet() instanceof FeatureLinkModelFacet) {
 		GenFeature metaFeature = ((FeatureLinkModelFacet) genLink.getModelFacet()).getMetaFeature();
 		outgoingClass = metaFeature.getGenClass();
 		createCommandNameInfix = metaFeature.getFeatureAccessorName();
 	} else {
 		continue;
 	}
 	if (!outgoingClass.getEcoreClass().isSuperTypeOf(genNode.getModelFacet().getMetaClass().getEcoreClass())) {
 		continue;
 	}
 
     stringBuffer.append(TEXT_380);
     stringBuffer.append(importManager.getImportedName(genLink.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_381);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditCommandsPackageName() + ".Create" + createCommandNameInfix + genLink.getVisualID() + "StartCommand"));
     stringBuffer.append(TEXT_382);
     
 }
 
     stringBuffer.append(TEXT_383);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.Command"));
     stringBuffer.append(TEXT_384);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.WrappingCommand"));
     stringBuffer.append(TEXT_385);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.util.TransactionUtil"));
     stringBuffer.append(TEXT_386);
     }/*when there's palette*/
     stringBuffer.append(TEXT_387);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.Command"));
     stringBuffer.append(TEXT_388);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.requests.CreateConnectionRequest"));
     stringBuffer.append(TEXT_389);
     if (palette != null && myHelper.hasIncomingLinks()) {
     stringBuffer.append(TEXT_390);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_391);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.requests.CreateConnectionRequestEx"));
     stringBuffer.append(TEXT_392);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.requests.CreateConnectionRequestEx"));
     stringBuffer.append(TEXT_393);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.requests.CreateConnectionRequestEx"));
     stringBuffer.append(TEXT_394);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_395);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_396);
     
 for(Iterator it = genDiagram.getLinks().iterator(); it.hasNext(); ) {
 	GenLink genLink = (GenLink)it.next();
 	if (!genLink.isViewDirectionAlignedWithModel() || genLink.getModelFacet() == null) {
 		continue;
 	}
 	GenClass incomingClass;
 	String createCommandNameInfix;
 	if (genLink.getModelFacet() instanceof TypeLinkModelFacet) {
 		TypeLinkModelFacet modelFacet = (TypeLinkModelFacet) genLink.getModelFacet();
 		incomingClass = modelFacet.getTargetMetaFeature().getTypeGenClass();
 		createCommandNameInfix = modelFacet.getMetaClass().getName();
 	} else if (genLink.getModelFacet() instanceof FeatureLinkModelFacet) {
 		GenFeature metaFeature = ((FeatureLinkModelFacet) genLink.getModelFacet()).getMetaFeature();
 		incomingClass = metaFeature.getTypeGenClass();
 		createCommandNameInfix = metaFeature.getFeatureAccessorName();
 	} else {
 		continue;
 	}
 	if (!incomingClass.getEcoreClass().isSuperTypeOf(genNode.getModelFacet().getMetaClass().getEcoreClass())) {
 		continue;
 	}
 
     stringBuffer.append(TEXT_397);
     stringBuffer.append(importManager.getImportedName(genLink.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_398);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditCommandsPackageName() + ".Create" + createCommandNameInfix + genLink.getVisualID() + "Command"));
     stringBuffer.append(TEXT_399);
     
 }
 
     stringBuffer.append(TEXT_400);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_401);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.WrappingCommand"));
     stringBuffer.append(TEXT_402);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.util.TransactionUtil"));
     stringBuffer.append(TEXT_403);
     }/*when there's palette*/
     stringBuffer.append(TEXT_404);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_405);
     
 if (myHelper.getPrimaryLabel() != null) {
 
     stringBuffer.append(TEXT_406);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPolicy"));
     stringBuffer.append(TEXT_407);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.policies.DelegatingDirectEditPolicy"));
     stringBuffer.append(TEXT_408);
     
 }
 
     {
 GenCommonBase genCommonBase = genNode;
     stringBuffer.append(TEXT_409);
     
 for (CustomBehaviour behaviour : genCommonBase.getBehaviour(CustomBehaviour.class)) {
 
     stringBuffer.append(TEXT_410);
     stringBuffer.append(behaviour.getKey());
     stringBuffer.append(TEXT_411);
     stringBuffer.append(importManager.getImportedName(behaviour.getEditPolicyQualifiedClassName()));
     stringBuffer.append(TEXT_412);
     }
     stringBuffer.append(TEXT_413);
     
 {
 	List<OpenDiagramBehaviour> behaviours = genCommonBase.getBehaviour(OpenDiagramBehaviour.class);
 	for(int i = 0, iMax = behaviours.size(); i < iMax; i++) {
 	/*doesn't make sense to install more than one policy for the same role*/ 
 		OpenDiagramBehaviour next = behaviours.get(i);
 
     stringBuffer.append(TEXT_414);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.policies.EditPolicyRoles"));
     stringBuffer.append(TEXT_415);
     if (i > 0) {
     stringBuffer.append(TEXT_416);
     stringBuffer.append(i+1);
     stringBuffer.append(TEXT_417);
     }
     stringBuffer.append(TEXT_418);
     stringBuffer.append(importManager.getImportedName(genCommonBase.getBehaviour(OpenDiagramBehaviour.class).get(0).getEditPolicyQualifiedClassName()));
     stringBuffer.append(TEXT_419);
     
 	}
 }
 
     }
     stringBuffer.append(TEXT_420);
     
 if (!genNode.getChildNodes().isEmpty()) {
 
     stringBuffer.append(TEXT_421);
     
 }
 
     
 if (myHelper.containsLinks()) {
 
     stringBuffer.append(TEXT_422);
     
 }
 
     stringBuffer.append(TEXT_423);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPolicy"));
     stringBuffer.append(TEXT_424);
     
 if (genNode.getPrimaryDragEditPolicyQualifiedClassName() != null) {
 
     stringBuffer.append(TEXT_425);
     stringBuffer.append(importManager.getImportedName(genNode.getPrimaryDragEditPolicyQualifiedClassName()));
     stringBuffer.append(TEXT_426);
     
 } else {
 	if (genNode.getCompartments().isEmpty()) {
 
     stringBuffer.append(TEXT_427);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.editpolicies.ResizableEditPolicy"));
     stringBuffer.append(TEXT_428);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.editpolicies.ResizableEditPolicy"));
     stringBuffer.append(TEXT_429);
     
 	} else {
 
     stringBuffer.append(TEXT_430);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.editpolicies.ResizableEditPolicy"));
     stringBuffer.append(TEXT_431);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.editpolicies.ResizableEditPolicy"));
     stringBuffer.append(TEXT_432);
     stringBuffer.append(importManager.getImportedName("java.util.List"));
     stringBuffer.append(TEXT_433);
     stringBuffer.append(importManager.getImportedName("java.util.List"));
     stringBuffer.append(TEXT_434);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_435);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_436);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_437);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_438);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_439);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_440);
     
 		for (Iterator compartments = genNode.getCompartments().iterator(); compartments.hasNext();){
 			GenCompartment next = (GenCompartment) compartments.next();
 
     stringBuffer.append(TEXT_441);
     stringBuffer.append(importManager.getImportedName(next.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_442);
     stringBuffer.append(importManager.getImportedName(next.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_443);
     
 		}
 
     stringBuffer.append(TEXT_444);
     
 	}
 	ResizeConstraints rc = (ResizeConstraints) genNode.getViewmap().find(ResizeConstraints.class);
 	if (rc != null) {
 		if (rc.getResizeHandles() == 0) {
 
     stringBuffer.append(TEXT_445);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.PositionConstants"));
     stringBuffer.append(TEXT_446);
     
 		} else {
 
     stringBuffer.append(TEXT_447);
     
 			for (Iterator it = rc.getResizeHandleNames().iterator(); it.hasNext(); ) {
 				String next = (String) it.next();
 
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.PositionConstants"));
     stringBuffer.append(TEXT_448);
     stringBuffer.append(next);
     
 				if (it.hasNext()) {
     stringBuffer.append(TEXT_449);
     
 				}
 			}
 		
     stringBuffer.append(TEXT_450);
     
 		}
 	}
 
     stringBuffer.append(TEXT_451);
     
 }
 
     stringBuffer.append(TEXT_452);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_453);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_454);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.Figure"));
     stringBuffer.append(TEXT_455);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.StackLayout"));
     stringBuffer.append(TEXT_456);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_457);
     
 	if (myHelper.hasBothChildrenAndCompartments()) {
 
     stringBuffer.append(TEXT_458);
     
 	}
 
     stringBuffer.append(TEXT_459);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_460);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.decorations.IDecorationManager"));
     stringBuffer.append(TEXT_461);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_462);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.decorations.PaneDecorationManager"));
     stringBuffer.append(TEXT_463);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.decorations.IDecorationManager"));
     stringBuffer.append(TEXT_464);
     
 	if (!genNode.getCompartments().isEmpty() && !genNode.getChildNodes().isEmpty()) {
 
     stringBuffer.append(TEXT_465);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_466);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_467);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.RectangleFigure"));
     stringBuffer.append(TEXT_468);
     
 	}
 
     stringBuffer.append(TEXT_469);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_470);
     
 String figureQualifiedClassName = null;
 Viewmap viewmap = genNode.getViewmap();
 if (viewmap instanceof FigureViewmap) {
 	figureQualifiedClassName = ((FigureViewmap) viewmap).getFigureQualifiedClassName();
 	if (figureQualifiedClassName == null || figureQualifiedClassName.trim().length() == 0) {
 		figureQualifiedClassName = "org.eclipse.draw2d.RectangleFigure";
 	}
 
     stringBuffer.append(TEXT_471);
     stringBuffer.append(importManager.getImportedName(figureQualifiedClassName));
     stringBuffer.append(TEXT_472);
     if (isXYLayout) {
     stringBuffer.append(TEXT_473);
     } /* use flow layout*/ 
     stringBuffer.append(TEXT_474);
     } else if (viewmap instanceof SnippetViewmap) {
     stringBuffer.append(TEXT_475);
     stringBuffer.append(((SnippetViewmap) viewmap).getBody());
     stringBuffer.append(TEXT_476);
     } else if (viewmap instanceof InnerClassViewmap) {
  	figureQualifiedClassName = ((InnerClassViewmap) viewmap).getClassName();
 
     stringBuffer.append(TEXT_477);
     stringBuffer.append(figureQualifiedClassName);
     stringBuffer.append(TEXT_478);
     stringBuffer.append(figureQualifiedClassName);
     stringBuffer.append(TEXT_479);
     if (!genNode.getChildNodes().isEmpty() && isXYLayout) { /*otherwise, leave to figure's default value*/
     stringBuffer.append(TEXT_480);
     }
     stringBuffer.append(TEXT_481);
     }
     stringBuffer.append(TEXT_482);
     if (figureQualifiedClassName != null) {
     stringBuffer.append(TEXT_483);
     stringBuffer.append(figureQualifiedClassName);
     stringBuffer.append(TEXT_484);
     stringBuffer.append(figureQualifiedClassName);
     stringBuffer.append(TEXT_485);
     }
     stringBuffer.append(TEXT_486);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_487);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.Figure"));
     stringBuffer.append(TEXT_488);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_489);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_490);
     
 if (!isXYLayout) {
 	String layoutClassName = importManager.getImportedName("org.eclipse.draw2d.ToolbarLayout");
 
     stringBuffer.append(TEXT_491);
     stringBuffer.append(layoutClassName);
     stringBuffer.append(TEXT_492);
     stringBuffer.append(layoutClassName);
     stringBuffer.append(TEXT_493);
     } else {
     stringBuffer.append(TEXT_494);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.FreeformLayout"));
     stringBuffer.append(TEXT_495);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_496);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.geometry.Rectangle"));
     stringBuffer.append(TEXT_497);
     }
     stringBuffer.append(TEXT_498);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_499);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_500);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_501);
     
 	if (myHelper.hasFixedChildren()) {
 
     stringBuffer.append(TEXT_502);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_503);
     
 for (Iterator it = myHelper.getInnerFixedLabels(); it.hasNext(); ) {
 	GenNodeLabel genLabel = (GenNodeLabel) it.next();
 	final String labelEditPart = importManager.getImportedName(genLabel.getEditPartQualifiedClassName());
 	final ParentAssignedViewmap childViewmap = (ParentAssignedViewmap) genLabel.getViewmap();
 	final String childSetterName = childViewmap.getSetterName() == null ? "setLabel" : childViewmap.getSetterName();
 
     stringBuffer.append(TEXT_504);
     stringBuffer.append(labelEditPart);
     stringBuffer.append(TEXT_505);
     stringBuffer.append(labelEditPart);
     stringBuffer.append(TEXT_506);
     stringBuffer.append(childSetterName);
     stringBuffer.append(TEXT_507);
     stringBuffer.append(childViewmap.getGetterName());
     stringBuffer.append(TEXT_508);
     
 }
 
 for (Iterator it = myHelper.getPinnedCompartments(); it.hasNext(); ) {
 	GenCompartment next = (GenCompartment) it.next();
 	final ParentAssignedViewmap childViewmap = (ParentAssignedViewmap) next.getViewmap();
 	String compartmentEditPartFQN = importManager.getImportedName(next.getEditPartQualifiedClassName());
 
     stringBuffer.append(TEXT_509);
     stringBuffer.append(compartmentEditPartFQN);
     stringBuffer.append(TEXT_510);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_511);
     stringBuffer.append(childViewmap.getGetterName());
     stringBuffer.append(TEXT_512);
     stringBuffer.append(compartmentEditPartFQN);
     stringBuffer.append(TEXT_513);
     	
 } // for pinned compartments
 
     stringBuffer.append(TEXT_514);
     
 //XXX: ignore labels assuming that they never may be removed
 for (Iterator it = myHelper.getPinnedCompartments(); it.hasNext(); ) {
 	GenCompartment next = (GenCompartment) it.next();
 	final ParentAssignedViewmap childViewmap = (ParentAssignedViewmap) next.getViewmap();
 	String compartmentEditPartFQN = importManager.getImportedName(next.getEditPartQualifiedClassName());
 
     stringBuffer.append(TEXT_515);
     stringBuffer.append(compartmentEditPartFQN);
     stringBuffer.append(TEXT_516);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_517);
     stringBuffer.append(childViewmap.getGetterName());
     stringBuffer.append(TEXT_518);
     stringBuffer.append(compartmentEditPartFQN);
     stringBuffer.append(TEXT_519);
     
 } // for pinned compartments
 
     stringBuffer.append(TEXT_520);
     
 } // if myHelper.hasFixedChildren()
 
     stringBuffer.append(TEXT_521);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_522);
     
 		if (myHelper.getPrimaryLabel() != null) {
 
     stringBuffer.append(TEXT_523);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_524);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_525);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_526);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_527);
     stringBuffer.append(importManager.getImportedName(myHelper.getPrimaryLabel().getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_528);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_529);
     
 		}
 
     stringBuffer.append(TEXT_530);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.Request"));
     stringBuffer.append(TEXT_531);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.RequestConstants"));
     stringBuffer.append(TEXT_532);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_533);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.requests.DirectEditRequest"));
     stringBuffer.append(TEXT_534);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.requests.DirectEditRequest"));
     stringBuffer.append(TEXT_535);
     stringBuffer.append(TEXT_536);
     stringBuffer.append(TEXT_537);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.RequestConstants"));
     stringBuffer.append(TEXT_538);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.Command"));
     stringBuffer.append(TEXT_539);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_540);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.requests.DirectEditRequest"));
     stringBuffer.append(TEXT_541);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_542);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_543);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_544);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_545);
     
 		for (Iterator it = myHelper.getAllLabels(); it.hasNext(); ) {
 			GenNodeLabel genLabel = (GenNodeLabel) it.next();
 			if (genLabel.isReadOnly()) {
 				continue;
 			}
 
     stringBuffer.append(TEXT_546);
     stringBuffer.append(importManager.getImportedName(genLabel.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_547);
     
 		}
 
     stringBuffer.append(TEXT_548);
     
 if (myHelper.hasExternalLabels()) {
 
     stringBuffer.append(TEXT_549);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_550);
     
 	for (Iterator externalLabels = myHelper.getExternalLabels(); externalLabels.hasNext();) {
 		GenNodeLabel next = (GenNodeLabel) externalLabels.next();
 
     stringBuffer.append(TEXT_551);
     stringBuffer.append(importManager.getImportedName(next.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_552);
     
 	}
 
     stringBuffer.append(TEXT_553);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_554);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.editparts.LayerManager"));
     stringBuffer.append(TEXT_555);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.editparts.LayerManager"));
     stringBuffer.append(TEXT_556);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditPartFactoryQualifiedClassName()));
     stringBuffer.append(TEXT_557);
     
 }
 if (!genNode.getChildNodes().isEmpty()) {
 
     stringBuffer.append(TEXT_558);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_559);
     
 	for(Iterator directChildren = genNode.getChildNodes().iterator(); directChildren.hasNext(); ) {
 		GenChildNode next = (GenChildNode) directChildren.next();
 
     stringBuffer.append(TEXT_560);
     stringBuffer.append(importManager.getImportedName(next.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_561);
     
 	}
 
     stringBuffer.append(TEXT_562);
     
 }
 if (myHelper.hasExternalLabels() || myHelper.hasFixedChildren() || myHelper.hasBothChildrenAndCompartments()) {
 
     stringBuffer.append(TEXT_563);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_564);
     
 	if (myHelper.hasExternalLabels()) {
 
     stringBuffer.append(TEXT_565);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_566);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.GraphicalEditPart"));
     stringBuffer.append(TEXT_567);
     
 	}
 	if (myHelper.hasFixedChildren()) {
 
     stringBuffer.append(TEXT_568);
     
 	}
 	if (myHelper.hasBothChildrenAndCompartments()) {
 
     stringBuffer.append(TEXT_569);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_570);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.GraphicalEditPart"));
     stringBuffer.append(TEXT_571);
     
 	}
 
     stringBuffer.append(TEXT_572);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_573);
     
 	if (myHelper.hasExternalLabels()) {
 
     stringBuffer.append(TEXT_574);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_575);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.GraphicalEditPart"));
     stringBuffer.append(TEXT_576);
     
 	}
 	if (myHelper.hasFixedChildren()){
 
     stringBuffer.append(TEXT_577);
     
 	}
 	if (myHelper.hasBothChildrenAndCompartments()) {
 
     stringBuffer.append(TEXT_578);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.IFigure"));
     stringBuffer.append(TEXT_579);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.GraphicalEditPart"));
     stringBuffer.append(TEXT_580);
     
 	}
 
     stringBuffer.append(TEXT_581);
     
 }
 
     
 if (myHelper.hasExternalLabels()) {
 
     stringBuffer.append(TEXT_582);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_583);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_584);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPart"));
     stringBuffer.append(TEXT_585);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.GraphicalEditPart"));
     stringBuffer.append(TEXT_586);
     
 }
 
     stringBuffer.append(TEXT_587);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Node"));
     stringBuffer.append(TEXT_588);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Bounds"));
     stringBuffer.append(TEXT_589);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Bounds"));
     stringBuffer.append(TEXT_590);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Bounds"));
     stringBuffer.append(TEXT_591);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.GraphicalEditPart"));
     stringBuffer.append(TEXT_592);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.geometry.Rectangle"));
     stringBuffer.append(TEXT_593);
     stringBuffer.append(importManager.getImportedName("java.util.List"));
     stringBuffer.append(TEXT_594);
     stringBuffer.append(importManager.getImportedName("java.util.List"));
     stringBuffer.append(TEXT_595);
     stringBuffer.append(importManager.getImportedName("java.util.List"));
     stringBuffer.append(TEXT_596);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.ConnectionAnchor"));
     stringBuffer.append(TEXT_597);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.ConnectionEditPart"));
     stringBuffer.append(TEXT_598);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.ChopboxAnchor"));
     stringBuffer.append(TEXT_599);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.ConnectionAnchor"));
     stringBuffer.append(TEXT_600);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.Request"));
     stringBuffer.append(TEXT_601);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.ChopboxAnchor"));
     stringBuffer.append(TEXT_602);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.ConnectionAnchor"));
     stringBuffer.append(TEXT_603);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.ConnectionEditPart"));
     stringBuffer.append(TEXT_604);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.ChopboxAnchor"));
     stringBuffer.append(TEXT_605);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.ConnectionAnchor"));
     stringBuffer.append(TEXT_606);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.Request"));
     stringBuffer.append(TEXT_607);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.ChopboxAnchor"));
     stringBuffer.append(TEXT_608);
     
 if (!genNode.getChildNodes().isEmpty() && isXYLayout) {
 
     stringBuffer.append(TEXT_609);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.SnapToHelper"));
     stringBuffer.append(TEXT_610);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.SnapToGrid"));
     stringBuffer.append(TEXT_611);
     
 }
 
     stringBuffer.append(TEXT_612);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.TreeEditPart"));
     stringBuffer.append(TEXT_613);
     /*@ include file="adapters/propertySource.javajetinc"*/
     stringBuffer.append(TEXT_614);
     
 if (!genNode.getChildNodes().isEmpty()) {
 
     stringBuffer.append(TEXT_615);
     
 }
 
     
 if (myHelper.containsLinks()) {
 
     stringBuffer.append(TEXT_616);
     
 }
 
     stringBuffer.append(TEXT_617);
     
 if (myHelper.containsLinks()) {
 
     stringBuffer.append(TEXT_618);
     
 }
 
     
 if (!genNode.getChildNodes().isEmpty()) {
 
     stringBuffer.append(TEXT_619);
     
 }
 
     stringBuffer.append(TEXT_620);
     
 {
 	final String _getViewCode = "getDiagramNode()";
 	final String _getDiagramCode = "getDiagramNode().getDiagram()";
 	final boolean _includeUncontainedLinks = false;
 	if (!genNode.getChildNodes().isEmpty()) {
 		final boolean isListLayout = !isXYLayout;
 		final List childNodes = genNode.getChildNodes();
 
     stringBuffer.append(TEXT_621);
     stringBuffer.append(TEXT_622);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.ChildNotationModelRefresher"));
     stringBuffer.append(TEXT_623);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.TransactionalEditingDomain"));
     stringBuffer.append(TEXT_624);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.util.TransactionUtil"));
     stringBuffer.append(TEXT_625);
     stringBuffer.append(_getDiagramCode);
     stringBuffer.append(TEXT_626);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.ChildNotationModelRefresher"));
     stringBuffer.append(TEXT_627);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.ChildNotationModelRefresher"));
     stringBuffer.append(TEXT_628);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.ChildNotationModelRefresher"));
     stringBuffer.append(TEXT_629);
     
 boolean hasConstraintsInChildren = false;
 for (Iterator it = childNodes.iterator(); it.hasNext(); ) {
 	GenNode nextNode = (GenNode) it.next();
 	TypeModelFacet typeModelFacet = nextNode.getModelFacet();
 	if (typeModelFacet != null && typeModelFacet.getMetaClass() != null && typeModelFacet.getModelElementSelector() != null) {
 		hasConstraintsInChildren = true;
 		break;
 	}
 }
 
     
 if (hasConstraintsInChildren) {
 
     stringBuffer.append(TEXT_630);
     stringBuffer.append(genDiagram.getVisualIDRegistryQualifiedClassName());
     stringBuffer.append(TEXT_631);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_632);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_633);
     
 }
 
     stringBuffer.append(TEXT_634);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_635);
     
 {
 	boolean hasDeclaredFilter = false;
 	Set genChildFeatures = new LinkedHashSet();
 	for (Iterator it = childNodes.iterator(); it.hasNext(); ) {
 		GenNode nextNode = (GenNode) it.next();
 		TypeModelFacet typeModelFacet = nextNode.getModelFacet();
 		if (typeModelFacet == null) {
 			continue;
 		}
 		GenFeature childMetaFeature = typeModelFacet.getChildMetaFeature();
 		if (genChildFeatures.contains(childMetaFeature)) {
 			continue;
 		}
 		genChildFeatures.add(childMetaFeature);
 		if (!hasDeclaredFilter) {
 			hasDeclaredFilter = true;
 
     stringBuffer.append(TEXT_636);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_637);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_638);
     stringBuffer.append(importManager.getImportedName(childMetaFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_639);
     stringBuffer.append(childMetaFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_640);
     
 		} else {
 
     stringBuffer.append(TEXT_641);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_642);
     stringBuffer.append(importManager.getImportedName(childMetaFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_643);
     stringBuffer.append(childMetaFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_644);
     
 		}
 	}	//for
 
     
 	if (hasDeclaredFilter) {
 
     stringBuffer.append(TEXT_645);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_646);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_647);
     
 	} else {
 		hasDeclaredFilter = true;
 
     stringBuffer.append(TEXT_648);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_649);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_650);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_651);
     
 	}
 
     
 	if (hasConstraintsInChildren) {
 		if (hasDeclaredFilter) {
 			hasDeclaredFilter = true;
 
     stringBuffer.append(TEXT_652);
     
 		} else {
 
     stringBuffer.append(TEXT_653);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_654);
     
 		}
 	}
 	if (!hasDeclaredFilter) {
 
     stringBuffer.append(TEXT_655);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_656);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_657);
     
 	}
 
     stringBuffer.append(TEXT_658);
     
 }	//local declaration of hasDeclaredFilter
 
     stringBuffer.append(TEXT_659);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.CreateNotationalElementCommand"));
     stringBuffer.append(TEXT_660);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.ElementDescriptor"));
     stringBuffer.append(TEXT_661);
     
 {
 	String _parentNode = "getHost()";
 
     stringBuffer.append(TEXT_662);
     stringBuffer.append(TEXT_663);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_664);
     
 for (Iterator it = childNodes.iterator(); it.hasNext(); ) {
 	GenNode nextNode = (GenNode) it.next();
 	TypeModelFacet typeModelFacet = nextNode.getModelFacet();
 	if (typeModelFacet == null) {
 		continue;
 	}
 	String childNodeInterfaceName = importManager.getImportedName(nextNode.getDomainMetaClass().getQualifiedInterfaceName());
 
     stringBuffer.append(TEXT_665);
     stringBuffer.append(importManager.getImportedName(nextNode.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_666);
     stringBuffer.append(childNodeInterfaceName);
     stringBuffer.append(TEXT_667);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.CreateNotationalNodeCommand"));
     stringBuffer.append(TEXT_668);
     stringBuffer.append(_parentNode);
     stringBuffer.append(TEXT_669);
     
 	if (!isListLayout) {
 		int defaultWidth = 40;
 		int defaultHeight = 40;
 		DefaultSizeAttributes defSizeAttrs = (DefaultSizeAttributes) nextNode.getViewmap().find(DefaultSizeAttributes.class);
 		if (defSizeAttrs != null) {
 			defaultWidth = defSizeAttrs.getWidth();
 			defaultHeight = defSizeAttrs.getHeight();
 		}
 
     stringBuffer.append(TEXT_670);
     stringBuffer.append(importManager.getImportedName("org.eclipse.draw2d.geometry.Rectangle"));
     stringBuffer.append(TEXT_671);
     stringBuffer.append(defaultWidth);
     stringBuffer.append(TEXT_672);
     stringBuffer.append(defaultHeight);
     stringBuffer.append(TEXT_673);
     
 	} else {
 
     stringBuffer.append(TEXT_674);
     
 	}
 
     stringBuffer.append(TEXT_675);
     stringBuffer.append(importManager.getImportedName(nextNode.getNotationViewFactoryQualifiedClassName()));
     stringBuffer.append(TEXT_676);
     
 }
 
     stringBuffer.append(TEXT_677);
     
 }
 
     stringBuffer.append(TEXT_678);
     stringBuffer.append(importManager.getImportedName("java.util.List"));
     stringBuffer.append(TEXT_679);
     stringBuffer.append(TEXT_680);
     
 if (childNodes.size() == 0) {
 
     stringBuffer.append(TEXT_681);
     stringBuffer.append(importManager.getImportedName("java.util.Collections"));
     stringBuffer.append(TEXT_682);
     
 } else {
 
     stringBuffer.append(TEXT_683);
     stringBuffer.append(importManager.getImportedName("java.util.List"));
     stringBuffer.append(TEXT_684);
     stringBuffer.append(importManager.getImportedName("java.util.LinkedList"));
     stringBuffer.append(TEXT_685);
     
 	Map genFeature2genNodeMap = new LinkedHashMap();
 	for (int nodeIndex = 0; nodeIndex < childNodes.size(); nodeIndex++) {
 		GenNode nextNode = (GenNode) childNodes.get(nodeIndex);
 		TypeModelFacet typeModelFacet = nextNode.getModelFacet();
 		if (typeModelFacet == null) {
 			continue;
 		}
 		GenFeature childMetaFeature = typeModelFacet.getChildMetaFeature();
 		if (!genFeature2genNodeMap.containsKey(childMetaFeature)) {
 			genFeature2genNodeMap.put(childMetaFeature, new ArrayList());
 		}
 		((Collection) genFeature2genNodeMap.get(childMetaFeature)).add(nextNode);
 	}
 	Set entrySet = genFeature2genNodeMap.entrySet();
 	if (entrySet.size() > 0) {
 
     stringBuffer.append(TEXT_686);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_687);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_688);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_689);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_690);
     
 	}
 	for (Iterator entries = entrySet.iterator(); entries.hasNext();) {
 		Map.Entry nextEntry = (Map.Entry) entries.next();
 		GenFeature childMetaFeature = (GenFeature) nextEntry.getKey();
 		Collection genNodesCollection = (Collection) nextEntry.getValue();
 		if (childMetaFeature.isListType()) {
 
     stringBuffer.append(TEXT_691);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_692);
     myFeatureGetAccessorHelper.appendFeatureValueGetter("modelObject", childMetaFeature, null, true);
     stringBuffer.append(TEXT_693);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_694);
     
 		} else {
 
     stringBuffer.append(TEXT_695);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_696);
     myFeatureGetAccessorHelper.appendFeatureValueGetter("modelObject", childMetaFeature, null, false);
     stringBuffer.append(TEXT_697);
     
 	}
 
     stringBuffer.append(TEXT_698);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_699);
     
 	boolean generateSwitch = genNodesCollection.size() != 1;
 	if (generateSwitch) {
 
     stringBuffer.append(TEXT_700);
     
 	}
 	for (Iterator genNodesIterator = genNodesCollection.iterator(); genNodesIterator.hasNext();) {
 		GenNode nextNode = (GenNode) genNodesIterator.next();
 		if (generateSwitch) {
 
     stringBuffer.append(TEXT_701);
     stringBuffer.append(importManager.getImportedName(nextNode.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_702);
     
 		} else {
 
     stringBuffer.append(TEXT_703);
     stringBuffer.append(importManager.getImportedName(nextNode.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_704);
     
 		}
 
     stringBuffer.append(TEXT_705);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.ElementDescriptor"));
     stringBuffer.append(TEXT_706);
     
 		if (generateSwitch) {
 
     stringBuffer.append(TEXT_707);
     
 		} else {
 
     stringBuffer.append(TEXT_708);
     
 		}
 	}
 	if (generateSwitch) {
 
     stringBuffer.append(TEXT_709);
     
 	}
 	if (childMetaFeature.isListType()) {
 
     stringBuffer.append(TEXT_710);
     
 	}
 }
 
     stringBuffer.append(TEXT_711);
     
 }
 
     stringBuffer.append(TEXT_712);
     stringBuffer.append(genDiagram.isSynchronized());
     stringBuffer.append(TEXT_713);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.ElementDescriptor"));
     stringBuffer.append(TEXT_714);
     stringBuffer.append(genDiagram.isSynchronized());
     stringBuffer.append(TEXT_715);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_716);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_717);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.AbstractNotationModelRefresher"));
     stringBuffer.append(TEXT_718);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.Command"));
     stringBuffer.append(TEXT_719);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.TransactionalEditingDomain"));
     stringBuffer.append(TEXT_720);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.util.TransactionUtil"));
     stringBuffer.append(TEXT_721);
     stringBuffer.append(_getDiagramCode);
     stringBuffer.append(TEXT_722);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.WrappingCommand"));
     stringBuffer.append(TEXT_723);
     
 	}
 
     stringBuffer.append(TEXT_724);
     
 if (myHelper.containsLinks() || _includeUncontainedLinks) {
 
     stringBuffer.append(TEXT_725);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.TransactionalEditingDomain"));
     stringBuffer.append(TEXT_726);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.util.TransactionUtil"));
     stringBuffer.append(TEXT_727);
     stringBuffer.append(_getDiagramCode);
     stringBuffer.append(TEXT_728);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.IViewService"));
     stringBuffer.append(TEXT_729);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.IViewService"));
     stringBuffer.append(TEXT_730);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.EditPartRegistryBasedViewService"));
     stringBuffer.append(TEXT_731);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.OwnedLinksNotationModelRefresher"));
     stringBuffer.append(TEXT_732);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.IViewService"));
     stringBuffer.append(TEXT_733);
     
 	boolean hasConstraintsInContainedLinks = false;
 	for (Iterator it = myHelper.getContainedTypeModelFacetLinks(); it.hasNext(); ) {
 		GenLink nextLink = (GenLink) it.next();
 		TypeModelFacet typeModelFacet = (TypeLinkModelFacet) nextLink.getModelFacet();
 		if (typeModelFacet != null && typeModelFacet.getMetaClass() != null && typeModelFacet.getModelElementSelector() != null) {
 			hasConstraintsInContainedLinks = true;
 			break;
 		}
 	}
 	if (hasConstraintsInContainedLinks) {
 
     stringBuffer.append(TEXT_734);
     stringBuffer.append(genDiagram.getVisualIDRegistryQualifiedClassName());
     stringBuffer.append(TEXT_735);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_736);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_737);
     
 	}
 	if (_includeUncontainedLinks) {
 
     stringBuffer.append(TEXT_738);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_739);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_740);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.notify.Notification"));
     stringBuffer.append(TEXT_741);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_742);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.notify.Notification"));
     stringBuffer.append(TEXT_743);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_744);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.notify.Notification"));
     stringBuffer.append(TEXT_745);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_746);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.notify.Notification"));
     stringBuffer.append(TEXT_747);
     
 	}
 
     stringBuffer.append(TEXT_748);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_749);
     
 	{
 		boolean hasDeclaredFilter = false;
 		Set genAffectingFeatures = new LinkedHashSet();
 		for(Iterator it = myHelper.getContainedTypeModelFacetLinks(); it.hasNext(); ) {
 			GenLink nextLink = (GenLink) it.next();
 			TypeLinkModelFacet modelFacet = (TypeLinkModelFacet) nextLink.getModelFacet();
 			if (modelFacet == null) {
 				continue;
 			}
 			GenFeature _feature = modelFacet.getChildMetaFeature();
 
     stringBuffer.append(TEXT_750);
     
 	/*
 	 * input: 
 	 * 		_feature: GenFeature
 	 * 		genAffectingFeatures : Set
 	 */
 	if (_feature == null || genAffectingFeatures.contains(_feature)) {
 		continue;
 	}
 	genAffectingFeatures.add(_feature);
 	if (!hasDeclaredFilter) {
 		hasDeclaredFilter = true;
 
     stringBuffer.append(TEXT_751);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_752);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_753);
     stringBuffer.append(importManager.getImportedName(_feature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_754);
     stringBuffer.append(_feature.getFeatureAccessorName());
     stringBuffer.append(TEXT_755);
     
 	} else {
 
     stringBuffer.append(TEXT_756);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_757);
     stringBuffer.append(importManager.getImportedName(_feature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_758);
     stringBuffer.append(_feature.getFeatureAccessorName());
     stringBuffer.append(TEXT_759);
     
 	}
 
     
 			_feature = modelFacet.getSourceMetaFeature();
 
     stringBuffer.append(TEXT_760);
     
 	/*
 	 * input: 
 	 * 		_feature: GenFeature
 	 * 		genAffectingFeatures : Set
 	 */
 	if (_feature == null || genAffectingFeatures.contains(_feature)) {
 		continue;
 	}
 	genAffectingFeatures.add(_feature);
 	if (!hasDeclaredFilter) {
 		hasDeclaredFilter = true;
 
     stringBuffer.append(TEXT_761);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_762);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_763);
     stringBuffer.append(importManager.getImportedName(_feature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_764);
     stringBuffer.append(_feature.getFeatureAccessorName());
     stringBuffer.append(TEXT_765);
     
 	} else {
 
     stringBuffer.append(TEXT_766);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_767);
     stringBuffer.append(importManager.getImportedName(_feature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_768);
     stringBuffer.append(_feature.getFeatureAccessorName());
     stringBuffer.append(TEXT_769);
     
 	}
 
     
 			_feature = modelFacet.getTargetMetaFeature();
 
     stringBuffer.append(TEXT_770);
     
 	/*
 	 * input: 
 	 * 		_feature: GenFeature
 	 * 		genAffectingFeatures : Set
 	 */
 	if (_feature == null || genAffectingFeatures.contains(_feature)) {
 		continue;
 	}
 	genAffectingFeatures.add(_feature);
 	if (!hasDeclaredFilter) {
 		hasDeclaredFilter = true;
 
     stringBuffer.append(TEXT_771);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_772);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_773);
     stringBuffer.append(importManager.getImportedName(_feature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_774);
     stringBuffer.append(_feature.getFeatureAccessorName());
     stringBuffer.append(TEXT_775);
     
 	} else {
 
     stringBuffer.append(TEXT_776);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_777);
     stringBuffer.append(importManager.getImportedName(_feature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_778);
     stringBuffer.append(_feature.getFeatureAccessorName());
     stringBuffer.append(TEXT_779);
     
 	}
 
     
 		}
 		for(Iterator it = myHelper.getContainedFeatureModelFacetLinks(); it.hasNext(); ) {
 			GenLink nextLink = (GenLink) it.next();
 			GenFeature _feature = ((FeatureLinkModelFacet) nextLink.getModelFacet()).getMetaFeature();
 
     stringBuffer.append(TEXT_780);
     
 	/*
 	 * input: 
 	 * 		_feature: GenFeature
 	 * 		genAffectingFeatures : Set
 	 */
 	if (_feature == null || genAffectingFeatures.contains(_feature)) {
 		continue;
 	}
 	genAffectingFeatures.add(_feature);
 	if (!hasDeclaredFilter) {
 		hasDeclaredFilter = true;
 
     stringBuffer.append(TEXT_781);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_782);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_783);
     stringBuffer.append(importManager.getImportedName(_feature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_784);
     stringBuffer.append(_feature.getFeatureAccessorName());
     stringBuffer.append(TEXT_785);
     
 	} else {
 
     stringBuffer.append(TEXT_786);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_787);
     stringBuffer.append(importManager.getImportedName(_feature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_788);
     stringBuffer.append(_feature.getFeatureAccessorName());
     stringBuffer.append(TEXT_789);
     
 	}
 
     
 		}
 		if (hasConstraintsInContainedLinks) {
 			if (hasDeclaredFilter) {
 
     stringBuffer.append(TEXT_790);
     
 			} else {
 				hasDeclaredFilter = true;
 
     stringBuffer.append(TEXT_791);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_792);
     
 			}
 		}
 		if (_includeUncontainedLinks) {
 			if (hasDeclaredFilter) {
 
     stringBuffer.append(TEXT_793);
     
 			} else {
 				hasDeclaredFilter = true;
 
     stringBuffer.append(TEXT_794);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_795);
     
 			}
 		}
 		if (!hasDeclaredFilter) {
 
     stringBuffer.append(TEXT_796);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_797);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.NotificationFilter"));
     stringBuffer.append(TEXT_798);
     
 		}
 	}	//local declaration of hasDeclaredFilter
 
     stringBuffer.append(TEXT_799);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.CreateNotationalElementCommand"));
     stringBuffer.append(TEXT_800);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.ElementDescriptor"));
     stringBuffer.append(TEXT_801);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.LinkDescriptor"));
     stringBuffer.append(TEXT_802);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.LinkDescriptor"));
     stringBuffer.append(TEXT_803);
     
 {
 	Iterator _containedTypeModelFacetLinks = myHelper.getContainedTypeModelFacetLinks();
 	Iterator _containedFeatureModelFacetLinks = myHelper.getContainedFeatureModelFacetLinks();
 	String _diagramCode = "getHost().getDiagram()";
 
     stringBuffer.append(TEXT_804);
     stringBuffer.append(TEXT_805);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_806);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_807);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.services.IViewDecorator"));
     stringBuffer.append(TEXT_808);
     
 	for(Iterator it = _containedTypeModelFacetLinks; it.hasNext(); ) {
 		GenLink nextLink = (GenLink) it.next();
 		TypeLinkModelFacet modelFacet = (TypeLinkModelFacet) nextLink.getModelFacet();
 		if (modelFacet == null) {
 			continue;
 		}
 
     stringBuffer.append(TEXT_809);
     stringBuffer.append(importManager.getImportedName(nextLink.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_810);
     stringBuffer.append(importManager.getImportedName(modelFacet.getMetaClass().getQualifiedInterfaceName()));
     stringBuffer.append(TEXT_811);
     stringBuffer.append(importManager.getImportedName(nextLink.getNotationViewFactoryQualifiedClassName()));
     stringBuffer.append(TEXT_812);
     
 	}
 	for(Iterator it = _containedFeatureModelFacetLinks; it.hasNext(); ) {
 		GenLink nextLink = (GenLink) it.next();
 
     stringBuffer.append(TEXT_813);
     stringBuffer.append(importManager.getImportedName(nextLink.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_814);
     stringBuffer.append(importManager.getImportedName(nextLink.getNotationViewFactoryQualifiedClassName()));
     stringBuffer.append(TEXT_815);
     
 	}
 
     stringBuffer.append(TEXT_816);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.CreateNotationalEdgeCommand"));
     stringBuffer.append(TEXT_817);
     stringBuffer.append(_diagramCode);
     stringBuffer.append(TEXT_818);
     
 }
 
     stringBuffer.append(TEXT_819);
     stringBuffer.append(importManager.getImportedName("java.util.List"));
     stringBuffer.append(TEXT_820);
     
 {
 	Iterator _containedTypeModelFacetLinks = myHelper.getContainedTypeModelFacetLinks();
 	Iterator _containedFeatureModelFacetLinks = myHelper.getContainedFeatureModelFacetLinks();
 
     stringBuffer.append(TEXT_821);
     stringBuffer.append(TEXT_822);
     stringBuffer.append(importManager.getImportedName("java.util.List"));
     stringBuffer.append(TEXT_823);
     stringBuffer.append(importManager.getImportedName("java.util.LinkedList"));
     stringBuffer.append(TEXT_824);
     
 Map genFeature2genLinkMap = new LinkedHashMap();
 for(Iterator it = _containedTypeModelFacetLinks; it.hasNext(); ) {
 	GenLink genLink = (GenLink)it.next();
 	TypeLinkModelFacet modelFacet = (TypeLinkModelFacet) genLink.getModelFacet();
 	GenFeature metaFeature = modelFacet.getChildMetaFeature();
 	if (!genFeature2genLinkMap.containsKey(metaFeature)) {
 		genFeature2genLinkMap.put(metaFeature, new ArrayList());
 	}
 	((Collection) genFeature2genLinkMap.get(metaFeature)).add(genLink);
 }
 Map genFeature2featureGenLinkMap = new LinkedHashMap();
 for(Iterator it = _containedFeatureModelFacetLinks; it.hasNext(); ) {
 	GenLink genLink = (GenLink)it.next();
 	GenFeature metaFeature = ((FeatureLinkModelFacet) genLink.getModelFacet()).getMetaFeature();
 	if (!genFeature2featureGenLinkMap.containsKey(metaFeature)) {
 		genFeature2featureGenLinkMap.put(metaFeature, new ArrayList());
 	}
 	((Collection) genFeature2featureGenLinkMap.get(metaFeature)).add(genLink);
 }
 if (!genFeature2genLinkMap.isEmpty() || !genFeature2featureGenLinkMap.isEmpty()) {
 
     stringBuffer.append(TEXT_825);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_826);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_827);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_828);
     
 }
 if (!genFeature2genLinkMap.isEmpty()) {
 
     stringBuffer.append(TEXT_829);
     
 }
 for (Iterator entries = genFeature2genLinkMap.entrySet().iterator(); entries.hasNext();) {
 	Map.Entry nextEntry = (Map.Entry) entries.next();
 	GenFeature metaFeature = (GenFeature) nextEntry.getKey();
 	Collection genLinksCollection = (Collection) nextEntry.getValue();
 	if (metaFeature.isListType()) {
 
     stringBuffer.append(TEXT_830);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_831);
     myFeatureGetAccessorHelper.appendFeatureValueGetter("modelObject", metaFeature, null, true);
     stringBuffer.append(TEXT_832);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_833);
     
 	} else {
 
     stringBuffer.append(TEXT_834);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_835);
     myFeatureGetAccessorHelper.appendFeatureValueGetter("modelObject", metaFeature, null, false);
     stringBuffer.append(TEXT_836);
     
 	}
 
     stringBuffer.append(TEXT_837);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_838);
     
 	boolean generateSwitch = genLinksCollection.size() != 1;
 	if (generateSwitch) {
 
     stringBuffer.append(TEXT_839);
     
 	}
 	for (Iterator genLinksIterator = genLinksCollection.iterator(); genLinksIterator.hasNext(); ) {
 		GenLink nextLink = (GenLink) genLinksIterator.next();
 		TypeLinkModelFacet modelFacet = (TypeLinkModelFacet) nextLink.getModelFacet();
 		if (generateSwitch) {
 
     stringBuffer.append(TEXT_840);
     stringBuffer.append(importManager.getImportedName(nextLink.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_841);
     
 		} else {
 
     stringBuffer.append(TEXT_842);
     stringBuffer.append(importManager.getImportedName(nextLink.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_843);
     
 		}
 		if (modelFacet.getSourceMetaFeature() != null) {
 
     stringBuffer.append(TEXT_844);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_845);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_846);
     myFeatureGetAccessorHelper.appendFeatureValueGetter("nextValue", modelFacet.getSourceMetaFeature(), null, false);
     stringBuffer.append(TEXT_847);
     
 		} else {
 
     stringBuffer.append(TEXT_848);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_849);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_850);
     
 		}
 		if (modelFacet.getTargetMetaFeature() != null) {
 
     stringBuffer.append(TEXT_851);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_852);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_853);
     myFeatureGetAccessorHelper.appendFeatureValueGetter("nextValue", modelFacet.getTargetMetaFeature(), null, false);
     stringBuffer.append(TEXT_854);
     
 		} else {
 
     stringBuffer.append(TEXT_855);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_856);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_857);
     
 		}
 
     stringBuffer.append(TEXT_858);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.LinkDescriptor"));
     stringBuffer.append(TEXT_859);
     
 		if (generateSwitch) {
 
     stringBuffer.append(TEXT_860);
     
 		} else {
 
     stringBuffer.append(TEXT_861);
     
 		}
 
     
 	}	//iterate over genLinksCollection
 	if (generateSwitch) {
 
     stringBuffer.append(TEXT_862);
     
 	}
 	if (metaFeature.isListType()) {
 
     stringBuffer.append(TEXT_863);
     
 	}
 }
 for (Iterator entries = genFeature2featureGenLinkMap.entrySet().iterator(); entries.hasNext();) {
 	Map.Entry nextEntry = (Map.Entry) entries.next();
 	GenFeature metaFeature = (GenFeature) nextEntry.getKey();
 	Collection genLinksCollection = (Collection) nextEntry.getValue();
 	if (metaFeature.isListType()) {
 
     stringBuffer.append(TEXT_864);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_865);
     myFeatureGetAccessorHelper.appendFeatureValueGetter("modelObject", metaFeature, null, true);
     stringBuffer.append(TEXT_866);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_867);
     
 	} else {
 
     stringBuffer.append(TEXT_868);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EObject"));
     stringBuffer.append(TEXT_869);
     myFeatureGetAccessorHelper.appendFeatureValueGetter("modelObject", metaFeature, null, false);
     stringBuffer.append(TEXT_870);
     
 	}
 
     stringBuffer.append(TEXT_871);
     
 	for (Iterator genLinksIterator = genLinksCollection.iterator(); genLinksIterator.hasNext(); ) {
 		GenLink nextLink = (GenLink) genLinksIterator.next();
 
     stringBuffer.append(TEXT_872);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.LinkDescriptor"));
     stringBuffer.append(TEXT_873);
     stringBuffer.append(importManager.getImportedName(nextLink.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_874);
     
 	}
 
     stringBuffer.append(TEXT_875);
     
 	if (metaFeature.isListType()) {
 
     stringBuffer.append(TEXT_876);
     
 	}
 }
 
     stringBuffer.append(TEXT_877);
     
 }
 
     stringBuffer.append(TEXT_878);
     stringBuffer.append(importManager.getImportedName("java.util.List"));
     stringBuffer.append(TEXT_879);
     stringBuffer.append(importManager.getImportedName("java.util.List"));
     stringBuffer.append(TEXT_880);
     stringBuffer.append(importManager.getImportedName("java.util.LinkedList"));
     stringBuffer.append(TEXT_881);
     stringBuffer.append(importManager.getImportedName("java.util.List"));
     stringBuffer.append(TEXT_882);
     stringBuffer.append(_getDiagramCode);
     stringBuffer.append(TEXT_883);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_884);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Edge"));
     stringBuffer.append(TEXT_885);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.Edge"));
     stringBuffer.append(TEXT_886);
     
 	if (_includeUncontainedLinks) {
 
     stringBuffer.append(TEXT_887);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_888);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_889);
     
 	}
 	if (myHelper.containsFeatureModelFacetLinks()) {
 
     stringBuffer.append(TEXT_890);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_891);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_892);
     
 		for(Iterator it = myHelper.getContainedFeatureModelFacetLinks(); it.hasNext(); ) {
 			GenLink nextLink = (GenLink) it.next();
 
     stringBuffer.append(TEXT_893);
     stringBuffer.append(importManager.getImportedName(nextLink.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_894);
     
 		}
 
     stringBuffer.append(TEXT_895);
     
 		if (myHelper.containsTypeModelFacetLinks()) {
 
     stringBuffer.append(TEXT_896);
     
 		} else {
 
     stringBuffer.append(TEXT_897);
     
 		}
 	}
 	if (myHelper.containsTypeModelFacetLinks()) {
 		if (!myHelper.containsFeatureModelFacetLinks()) {
 
     stringBuffer.append(TEXT_898);
     
 		}
 
     stringBuffer.append(TEXT_899);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_900);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_901);
     
 		for(Iterator it = myHelper.getContainedTypeModelFacetLinks(); it.hasNext(); ) {
 			GenLink nextLink = (GenLink) it.next();
 
     stringBuffer.append(TEXT_902);
     stringBuffer.append(importManager.getImportedName(nextLink.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_903);
     
 		}
 
     stringBuffer.append(TEXT_904);
     
 	}
 
     stringBuffer.append(TEXT_905);
     stringBuffer.append(genDiagram.isSynchronized());
     stringBuffer.append(TEXT_906);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.ElementDescriptor"));
     stringBuffer.append(TEXT_907);
     stringBuffer.append(genDiagram.isSynchronized());
     stringBuffer.append(TEXT_908);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_909);
     stringBuffer.append(_getViewCode);
     stringBuffer.append(TEXT_910);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.canonical.AbstractNotationModelRefresher"));
     stringBuffer.append(TEXT_911);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.Command"));
     stringBuffer.append(TEXT_912);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.TransactionalEditingDomain"));
     stringBuffer.append(TEXT_913);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.util.TransactionUtil"));
     stringBuffer.append(TEXT_914);
     stringBuffer.append(_getDiagramCode);
     stringBuffer.append(TEXT_915);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.WrappingCommand"));
     stringBuffer.append(TEXT_916);
     
 }
 
     
 }	//end of local declarations
 
     stringBuffer.append(TEXT_917);
     stringBuffer.append(TEXT_918);
     stringBuffer.append(TEXT_919);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.RefreshAdapter"));
     stringBuffer.append(TEXT_920);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.RefreshAdapter"));
     stringBuffer.append(TEXT_921);
     stringBuffer.append(TEXT_922);
     stringBuffer.append(TEXT_923);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EStructuralFeature"));
     stringBuffer.append(TEXT_924);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EStructuralFeature"));
     stringBuffer.append(TEXT_925);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EStructuralFeature"));
     stringBuffer.append(TEXT_926);
     
 	final String primaryView = "getDiagramNode()";
 
     stringBuffer.append(TEXT_927);
     
 boolean isFixedFontSetInFigure;
 {
 	StyleAttributes styleAttributes = (genElement.getViewmap() == null) ? null : (StyleAttributes)genElement.getViewmap().find(StyleAttributes.class);
 	isFixedFontSetInFigure = styleAttributes != null && styleAttributes.isFixedFont();
 }
 
     stringBuffer.append(TEXT_928);
     
 if (!isFixedFontSetInFigure) {
 
     stringBuffer.append(TEXT_929);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.FontStyle"));
     stringBuffer.append(TEXT_930);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.FontStyle"));
     stringBuffer.append(TEXT_931);
     stringBuffer.append(primaryView);
     stringBuffer.append(TEXT_932);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_933);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.graphics.Font"));
     stringBuffer.append(TEXT_934);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.SWT"));
     stringBuffer.append(TEXT_935);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.SWT"));
     stringBuffer.append(TEXT_936);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.SWT"));
     stringBuffer.append(TEXT_937);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.graphics.Font"));
     stringBuffer.append(TEXT_938);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.graphics.FontData"));
     stringBuffer.append(TEXT_939);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.graphics.Font"));
     stringBuffer.append(TEXT_940);
     
 }
 
     stringBuffer.append(TEXT_941);
     
 if (!isFixedFontSetInFigure) {
 
     stringBuffer.append(TEXT_942);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.graphics.Font"));
     stringBuffer.append(TEXT_943);
     
 }
 
     stringBuffer.append(TEXT_944);
     stringBuffer.append(TEXT_945);
     stringBuffer.append(TEXT_946);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.LineStyle"));
     stringBuffer.append(TEXT_947);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.LineStyle"));
     stringBuffer.append(TEXT_948);
     stringBuffer.append(primaryView);
     stringBuffer.append(TEXT_949);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_950);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.graphics.Color"));
     stringBuffer.append(TEXT_951);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.graphics.Color"));
     stringBuffer.append(TEXT_952);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.graphics.Color"));
     stringBuffer.append(TEXT_953);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.graphics.Color"));
     stringBuffer.append(TEXT_954);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.FillStyle"));
     stringBuffer.append(TEXT_955);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.FillStyle"));
     stringBuffer.append(TEXT_956);
     stringBuffer.append(primaryView);
     stringBuffer.append(TEXT_957);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_958);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.graphics.Color"));
     stringBuffer.append(TEXT_959);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.graphics.Color"));
     stringBuffer.append(TEXT_960);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.graphics.Color"));
     stringBuffer.append(TEXT_961);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.graphics.Color"));
     stringBuffer.append(TEXT_962);
     stringBuffer.append(TEXT_963);
     stringBuffer.append(TEXT_964);
     stringBuffer.append(importManager.getImportedName("java.util.HashMap"));
     stringBuffer.append(TEXT_965);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EStructuralFeature"));
     stringBuffer.append(TEXT_966);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.notify.Notification"));
     stringBuffer.append(TEXT_967);
     stringBuffer.append(importManager.getImportedName("java.util.HashMap"));
     stringBuffer.append(TEXT_968);
     stringBuffer.append(TEXT_969);
     stringBuffer.append(TEXT_970);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_971);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_972);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_973);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_974);
     stringBuffer.append(TEXT_975);
     stringBuffer.append(TEXT_976);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_977);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_978);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_979);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_980);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_981);
     stringBuffer.append(TEXT_982);
     stringBuffer.append(TEXT_983);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_984);
     stringBuffer.append(TEXT_985);
     stringBuffer.append(TEXT_986);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_987);
     stringBuffer.append(TEXT_988);
     stringBuffer.append(TEXT_989);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_990);
     stringBuffer.append(TEXT_991);
     stringBuffer.append(TEXT_992);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_993);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_994);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_995);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_996);
     stringBuffer.append(TEXT_997);
     stringBuffer.append(TEXT_998);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_999);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_1000);
     
 if (genNode.getViewmap() instanceof InnerClassViewmap) {
 	String classBody = ((InnerClassViewmap) genNode.getViewmap()).getClassBody();
 
     stringBuffer.append(TEXT_1001);
     stringBuffer.append(classBody);
     stringBuffer.append(TEXT_1002);
     
 if (classBody.indexOf("DPtoLP") != -1) {
 
     stringBuffer.append(TEXT_1003);
     
 }
 
     
 }
 
     stringBuffer.append(TEXT_1004);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.tree.BaseTreeEditPart"));
     stringBuffer.append(TEXT_1005);
     stringBuffer.append(importManager.getImportedName(genDiagram.getEditorGen().getPlugin().getActivatorQualifiedClassName()));
     stringBuffer.append(TEXT_1006);
     stringBuffer.append(TEXT_1007);
     stringBuffer.append(TEXT_1008);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPolicy"));
     stringBuffer.append(TEXT_1009);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.editpolicies.ComponentEditPolicy"));
     stringBuffer.append(TEXT_1010);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.Command"));
     stringBuffer.append(TEXT_1011);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.requests.GroupRequest"));
     stringBuffer.append(TEXT_1012);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.TransactionalEditingDomain"));
     stringBuffer.append(TEXT_1013);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.util.TransactionUtil"));
     stringBuffer.append(TEXT_1014);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_1015);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_1016);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.RemoveNotationalElementCommand"));
     stringBuffer.append(TEXT_1017);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_1018);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.WrappingCommand"));
     stringBuffer.append(TEXT_1019);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.TransactionalEditingDomain"));
     stringBuffer.append(TEXT_1020);
     
 {
 TypeModelFacet facet = genNode.getModelFacet();
 GenFeature childFeature = facet.getChildMetaFeature();
 GenFeature containmentFeature = facet.getContainmentMetaFeature();
 if (childFeature != null && childFeature != containmentFeature && !childFeature.isDerived()) {
 
     stringBuffer.append(TEXT_1021);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_1022);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_1023);
     
 	if (containmentFeature.getEcoreFeature().isMany()) {
 
     stringBuffer.append(TEXT_1024);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.RemoveCommand"));
     stringBuffer.append(TEXT_1025);
     stringBuffer.append(importManager.getImportedName(containmentFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_1026);
     stringBuffer.append(containmentFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_1027);
     
 	} else {
 
     stringBuffer.append(TEXT_1028);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_1029);
     stringBuffer.append(importManager.getImportedName(containmentFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_1030);
     stringBuffer.append(containmentFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_1031);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_1032);
     
 	}
 
     
 	if (childFeature.getEcoreFeature().isMany()) {
 
     stringBuffer.append(TEXT_1033);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.RemoveCommand"));
     stringBuffer.append(TEXT_1034);
     stringBuffer.append(importManager.getImportedName(childFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_1035);
     stringBuffer.append(childFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_1036);
     
 	} else {
 
     stringBuffer.append(TEXT_1037);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_1038);
     stringBuffer.append(importManager.getImportedName(childFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_1039);
     stringBuffer.append(childFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_1040);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_1041);
     
 	}
 
     stringBuffer.append(TEXT_1042);
     
 } else {
 	if (containmentFeature.getEcoreFeature().isMany()) {
 
     stringBuffer.append(TEXT_1043);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.RemoveCommand"));
     stringBuffer.append(TEXT_1044);
     stringBuffer.append(importManager.getImportedName(containmentFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_1045);
     stringBuffer.append(containmentFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_1046);
     
 	} else {
 
     stringBuffer.append(TEXT_1047);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_1048);
     stringBuffer.append(importManager.getImportedName(containmentFeature.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_1049);
     stringBuffer.append(containmentFeature.getFeatureAccessorName());
     stringBuffer.append(TEXT_1050);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_1051);
     
 	}
 }
 
     stringBuffer.append(TEXT_1052);
     
 }	/*restrict local vars used in component edit policy*/
 
     
 if (myHelper.getPrimaryLabel() != null) {
 		String editPatternCode = importManager.getImportedName(myHelper.getPrimaryLabel().getEditPartQualifiedClassName()) + ".EDIT_PATTERN";
 		String resolvedSemanticElement = "(" + importManager.getImportedName(genNode.getDomainMetaClass().getQualifiedInterfaceName()) + ") getDiagramNode().getElement()";
 		LabelModelFacet labelModelFacet = myHelper.getPrimaryLabel().getModelFacet();
 		GenClass underlyingMetaClass = genNode.getDomainMetaClass();
 
     stringBuffer.append(TEXT_1053);
     stringBuffer.append(TEXT_1054);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.EditPolicy"));
     stringBuffer.append(TEXT_1055);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.editpolicies.DirectEditPolicy"));
     stringBuffer.append(TEXT_1056);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.requests.DirectEditRequest"));
     stringBuffer.append(TEXT_1057);
     stringBuffer.append(TEXT_1058);
     stringBuffer.append(TEXT_1059);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.Command"));
     stringBuffer.append(TEXT_1060);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.requests.DirectEditRequest"));
     stringBuffer.append(TEXT_1061);
     
 if (labelModelFacet instanceof FeatureLabelModelFacet == false) {
 
     stringBuffer.append(TEXT_1062);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_1063);
     
 } else {
 	FeatureLabelModelFacet featureLabelModelFacet = (FeatureLabelModelFacet) labelModelFacet;
 	List metaFeatures = featureLabelModelFacet.getMetaFeatures();
 
     stringBuffer.append(TEXT_1064);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_1065);
     
 	if (metaFeatures.size() == 1 && String.class.equals(((GenFeature) metaFeatures.get(0)).getEcoreFeature().getEType().getInstanceClass())) {
 
     stringBuffer.append(TEXT_1066);
     
 	}
 
     stringBuffer.append(TEXT_1067);
     stringBuffer.append(importManager.getImportedName("java.text.MessageFormat"));
     stringBuffer.append(TEXT_1068);
     stringBuffer.append(editPatternCode);
     stringBuffer.append(TEXT_1069);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_1070);
     stringBuffer.append(importManager.getImportedName("java.text.ParseException"));
     stringBuffer.append(TEXT_1071);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_1072);
     
 	if (metaFeatures.size() == 1 && String.class.equals(((GenFeature) metaFeatures.get(0)).getEcoreFeature().getEType().getInstanceClass())) {
 
     stringBuffer.append(TEXT_1073);
     
 	}
 
     stringBuffer.append(TEXT_1074);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.TransactionalEditingDomain"));
     stringBuffer.append(TEXT_1075);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.util.TransactionUtil"));
     stringBuffer.append(TEXT_1076);
     stringBuffer.append(primaryView);
     stringBuffer.append(TEXT_1077);
     stringBuffer.append(metaFeatures.size());
     stringBuffer.append(TEXT_1078);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.commands.UnexecutableCommand"));
     stringBuffer.append(TEXT_1079);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.Command"));
     stringBuffer.append(TEXT_1080);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.commands.WrappingCommand"));
     stringBuffer.append(TEXT_1081);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.Command"));
     stringBuffer.append(TEXT_1082);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.transaction.TransactionalEditingDomain"));
     stringBuffer.append(TEXT_1083);
     stringBuffer.append(importManager.getImportedName(underlyingMetaClass.getQualifiedInterfaceName()));
     stringBuffer.append(TEXT_1084);
     stringBuffer.append(resolvedSemanticElement);
     stringBuffer.append(TEXT_1085);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_1086);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.CompoundCommand"));
     stringBuffer.append(TEXT_1087);
     
 	boolean haveDeclaredValues = false;
 	for(int i = 0; i < metaFeatures.size(); i++) {
 		GenFeature nextFeatureToSet = (GenFeature) metaFeatures.get(i);
 		EStructuralFeature nextEcoreFeature = nextFeatureToSet.getEcoreFeature();
 
     stringBuffer.append(TEXT_1088);
     if (i == 0) {
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EAttribute"));
     stringBuffer.append(TEXT_1089);
     }
     stringBuffer.append(TEXT_1090);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.ecore.EAttribute"));
     stringBuffer.append(TEXT_1091);
     stringBuffer.append(importManager.getImportedName(nextFeatureToSet.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_1092);
     stringBuffer.append(nextFeatureToSet.getFeatureAccessorName());
     stringBuffer.append(TEXT_1093);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.services.ParserUtil"));
     stringBuffer.append(TEXT_1094);
     stringBuffer.append(i);
     stringBuffer.append(TEXT_1095);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.command.UnexecutableCommand"));
     stringBuffer.append(TEXT_1096);
     
 		if (nextEcoreFeature.isMany()) {
 
     stringBuffer.append(TEXT_1097);
     if (!haveDeclaredValues) { haveDeclaredValues = true;
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.util.EList"));
     stringBuffer.append(TEXT_1098);
     }
     stringBuffer.append(TEXT_1099);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.common.util.BasicEList"));
     stringBuffer.append(TEXT_1100);
     stringBuffer.append(nextFeatureToSet.getAccessorName());
     stringBuffer.append(TEXT_1101);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.RemoveCommand"));
     stringBuffer.append(TEXT_1102);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.AddCommand"));
     stringBuffer.append(TEXT_1103);
     
 		} else {
 
     stringBuffer.append(TEXT_1104);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_1105);
     stringBuffer.append(importManager.getImportedName("org.eclipse.emf.edit.command.SetCommand"));
     stringBuffer.append(TEXT_1106);
     
 		}
 
     
 	}
 
     stringBuffer.append(TEXT_1107);
     
 }
 
     stringBuffer.append(TEXT_1108);
     
 }
 
     stringBuffer.append(TEXT_1109);
     
 if (myHelper.getPrimaryLabel() != null) {
 
     stringBuffer.append(TEXT_1110);
     stringBuffer.append(TEXT_1111);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.services.TreeDirectEditManager"));
     stringBuffer.append(TEXT_1112);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.Request"));
     stringBuffer.append(TEXT_1113);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.RequestConstants"));
     stringBuffer.append(TEXT_1114);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.services.TreeDirectEditManager"));
     stringBuffer.append(TEXT_1115);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.services.TreeDirectEditManager"));
     stringBuffer.append(TEXT_1116);
     stringBuffer.append(importManager.getImportedName("org.eclipse.jface.viewers.TextCellEditor"));
     stringBuffer.append(TEXT_1117);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gef.tools.CellEditorLocator"));
     stringBuffer.append(TEXT_1118);
     stringBuffer.append(importManager.getImportedName("org.eclipse.jface.viewers.CellEditor"));
     stringBuffer.append(TEXT_1119);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.widgets.TreeItem"));
     stringBuffer.append(TEXT_1120);
     stringBuffer.append(importManager.getImportedName("org.eclipse.swt.widgets.TreeItem"));
     stringBuffer.append(TEXT_1121);
     stringBuffer.append(importManager.getImportedName(myHelper.getPrimaryLabel().getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_1122);
     
 }
 
     stringBuffer.append(TEXT_1123);
     stringBuffer.append(TEXT_1124);
     stringBuffer.append(TEXT_1125);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.RefreshAdapter"));
     stringBuffer.append(TEXT_1126);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.lite.edit.parts.update.RefreshAdapter"));
     stringBuffer.append(TEXT_1127);
     
 if (myHelper.getPrimaryLabel() != null) {
 
     stringBuffer.append(TEXT_1128);
     stringBuffer.append(importManager.getImportedName(myHelper.getPrimaryLabel().getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_1129);
     stringBuffer.append(importManager.getImportedName(myHelper.getPrimaryLabel().getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_1130);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_1131);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_1132);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_1133);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_1134);
     stringBuffer.append(importManager.getImportedName(myHelper.getPrimaryLabel().getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_1135);
     stringBuffer.append(importManager.getImportedName(myHelper.getPrimaryLabel().getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_1136);
     stringBuffer.append(genNode.getEditPartClassName());
     stringBuffer.append(TEXT_1137);
     
 }
 
     stringBuffer.append(TEXT_1138);
     
 if (myHelper.getPrimaryLabel() != null) {
 	LabelModelFacet labelModelFacet = myHelper.getPrimaryLabel().getModelFacet();
 	if (labelModelFacet instanceof FeatureLabelModelFacet) {
 		FeatureLabelModelFacet featureLabelModelFacet = (FeatureLabelModelFacet) labelModelFacet;
 		for(Iterator it = featureLabelModelFacet.getMetaFeatures().iterator(); it.hasNext(); ) {
 			GenFeature next = (GenFeature) it.next();
 
     stringBuffer.append(TEXT_1139);
     stringBuffer.append(importManager.getImportedName(next.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_1140);
     stringBuffer.append(next.getFeatureAccessorName());
     stringBuffer.append(TEXT_1141);
     
 		}
 	}
 } else {
 	GenClass metaClass = genNode.getDomainMetaClass();
 	if (metaClass != null) {
 		List labelNotifyFeatures = metaClass.getLabelNotifyFeatures();
 		for(Iterator it = labelNotifyFeatures.iterator(); it.hasNext(); ) {
 			GenFeature next = (GenFeature) it.next();
 
     stringBuffer.append(TEXT_1142);
     stringBuffer.append(importManager.getImportedName(next.getGenPackage().getQualifiedPackageInterfaceName()));
     stringBuffer.append(TEXT_1143);
     stringBuffer.append(next.getFeatureAccessorName());
     stringBuffer.append(TEXT_1144);
     
 		}
 	}
 
     
 }
 
     stringBuffer.append(TEXT_1145);
     stringBuffer.append(importManager.getImportedName("java.util.List"));
     stringBuffer.append(TEXT_1146);
     stringBuffer.append(importManager.getImportedName("java.util.List"));
     stringBuffer.append(TEXT_1147);
     stringBuffer.append(importManager.getImportedName("java.util.ArrayList"));
     stringBuffer.append(TEXT_1148);
     stringBuffer.append(importManager.getImportedName("java.util.Iterator"));
     stringBuffer.append(TEXT_1149);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_1150);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.View"));
     stringBuffer.append(TEXT_1151);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.DrawerStyle"));
     stringBuffer.append(TEXT_1152);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.DrawerStyle"));
     stringBuffer.append(TEXT_1153);
     stringBuffer.append(importManager.getImportedName("org.eclipse.gmf.runtime.notation.NotationPackage"));
     stringBuffer.append(TEXT_1154);
     stringBuffer.append(importManager.getImportedName(genDiagram.getVisualIDRegistryQualifiedClassName()));
     stringBuffer.append(TEXT_1155);
     
 	for(Iterator it = genNode.getChildNodes().iterator(); it.hasNext(); ) {
 		GenChildNode next = (GenChildNode)it.next();
 
     stringBuffer.append(TEXT_1156);
     stringBuffer.append(importManager.getImportedName(next.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_1157);
     
 }
 
     
 		for (Iterator compartments = genNode.getCompartments().iterator(); compartments.hasNext();){
 			GenCompartment next = (GenCompartment) compartments.next();
 
     stringBuffer.append(TEXT_1158);
     stringBuffer.append(importManager.getImportedName(next.getEditPartQualifiedClassName()));
     stringBuffer.append(TEXT_1159);
     
 }
 
     stringBuffer.append(TEXT_1160);
     importManager.emitSortedImports();
     stringBuffer.append(TEXT_1161);
     return stringBuffer.toString();
   }
 }
