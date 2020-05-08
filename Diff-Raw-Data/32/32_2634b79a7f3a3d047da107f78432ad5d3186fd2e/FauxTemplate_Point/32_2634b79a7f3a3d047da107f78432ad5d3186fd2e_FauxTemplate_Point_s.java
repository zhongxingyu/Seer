 package com.dafrito.rfe.script.proxies;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.dafrito.rfe.geom.points.Point;
 import com.dafrito.rfe.geom.points.Point_Euclidean;
 import com.dafrito.rfe.gui.debug.Debugger;
 import com.dafrito.rfe.inspect.Nodeable;
 import com.dafrito.rfe.script.ScriptConvertible;
 import com.dafrito.rfe.script.ScriptEnvironment;
 import com.dafrito.rfe.script.exceptions.Exception_Nodeable;
 import com.dafrito.rfe.script.parsing.Parser;
 import com.dafrito.rfe.script.parsing.Referenced;
 import com.dafrito.rfe.script.parsing.ScriptKeywordType;
 import com.dafrito.rfe.script.values.ScriptFunction;
 import com.dafrito.rfe.script.values.ScriptTemplate;
 import com.dafrito.rfe.script.values.ScriptTemplate_Abstract;
 import com.dafrito.rfe.script.values.ScriptValue;
 import com.dafrito.rfe.script.values.ScriptValueType;
 import com.dafrito.rfe.script.values.ScriptValue_Faux;
 
 public class FauxTemplate_Point extends FauxTemplate implements ScriptConvertible<Point>, Nodeable {
 	public static final String POINTSTRING = "Point";
 	public Point point;
 
 	public FauxTemplate_Point(ScriptEnvironment env) {
 		super(env, ScriptValueType.createType(env, POINTSTRING), ScriptValueType.getObjectType(env), new LinkedList<ScriptValueType>(), false);
 	}
 
 	public FauxTemplate_Point(ScriptEnvironment env, ScriptValueType type) {
 		super(env, type);
 		this.point = new Point_Euclidean(env, 0, 0, 0);
 	}
 
 	public FauxTemplate_Point(ScriptEnvironment env, ScriptValueType type, ScriptValueType extended, List<ScriptValueType> implemented, boolean isAbstract) {
 		super(env, type, extended, implemented, isAbstract);
 	}
 
 	// ScriptConvertible and Nodeable implementations
 	@Override
 	public Point convert() {
 		return this.point;
 	}
 
 	// Function bodies are contained via a series of if statements in execute
 	// Template will be null if the object is exactly of this type and is constructing, and thus must be created then
 	@Override
 	public ScriptValue execute(Referenced ref, String name, List<ScriptValue> params, ScriptTemplate_Abstract rawTemplate) throws Exception_Nodeable {
 		assert Debugger.openNode("Faux Template Executions", "Executing Point Faux Template Function (" + ScriptFunction.getDisplayableFunctionName(name) + ")");
 		FauxTemplate_Point template = (FauxTemplate_Point) rawTemplate;
 		ScriptValue returning = null;
 		assert Debugger.addSnapNode("Template provided", template);
 		assert Debugger.addSnapNode("Parameters provided", params);
		if (name == null || name.equals("")) {
 			if (template == null) {
 				template = (FauxTemplate_Point) this.createObject(ref, template);
 			}
 			if (params.size() == 3) {
 				template.getPoint().setX(Parser.getDouble(params.get(0)).doubleValue());
 				template.getPoint().setY(Parser.getDouble(params.get(1)).doubleValue());
 				template.getPoint().setZ(Parser.getDouble(params.get(2)).doubleValue());
 			}
 			params.clear();
 			returning = this.getExtendedFauxClass().execute(ref, name, params, template);
 		}
 		if (name.equals("getX")) {
 			returning = Parser.getRiffDouble(this.getEnvironment(), (template).getPoint().getX());
 		} else if (name.equals("getY")) {
 			returning = Parser.getRiffDouble(this.getEnvironment(), (template).getPoint().getY());
 		} else if (name.equals("getZ")) {
 			returning = Parser.getRiffDouble(this.getEnvironment(), (template).getPoint().getZ());
 		} else if (name.equals("setX")) {
 			(template).getPoint().setX(Parser.getDouble(params.get(0)).doubleValue());
 		} else if (name.equals("setY")) {
 			(template).getPoint().setY(Parser.getDouble(params.get(0)).doubleValue());
 		} else if (name.equals("setZ")) {
 			(template).getPoint().setZ(Parser.getDouble(params.get(0)).doubleValue());
 		} else {
 			returning = this.getExtendedFauxClass().execute(ref, name, params, template);
 		}
 		assert Debugger.closeNode();
 		return returning;
 	}
 
 	public Point getPoint() {
 		return this.point;
 	}
 
 	// addFauxFunction(name,ScriptValueType type,List<ScriptValue_Abstract>params,ScriptKeywordType permission,boolean isAbstract)
 	// All functions must be defined here. All function bodies are defined in 'execute'.
 	@Override
 	public void initialize() throws Exception_Nodeable {
 		assert Debugger.openNode("Faux Template Initializations", "Initializing point faux template");
 		this.addConstructor(this.getType());
 		List<ScriptValue> fxnParams = new LinkedList<ScriptValue>();
 		fxnParams.add(new ScriptValue_Faux(this.getEnvironment(), ScriptValueType.DOUBLE));
 		fxnParams.add(new ScriptValue_Faux(this.getEnvironment(), ScriptValueType.DOUBLE));
 		fxnParams.add(new ScriptValue_Faux(this.getEnvironment(), ScriptValueType.DOUBLE));
 		this.addConstructor(this.getType(), fxnParams);
 		this.disableFullCreation();
 		this.getExtendedClass().initialize();
 		this.addFauxFunction("getX", ScriptValueType.DOUBLE, Collections.<ScriptValue> emptyList(), ScriptKeywordType.PUBLIC, false, false);
 		this.addFauxFunction("getY", ScriptValueType.DOUBLE, Collections.<ScriptValue> emptyList(), ScriptKeywordType.PUBLIC, false, false);
 		this.addFauxFunction("getZ", ScriptValueType.DOUBLE, Collections.<ScriptValue> emptyList(), ScriptKeywordType.PUBLIC, false, false);
 		fxnParams = new LinkedList<ScriptValue>();
 		fxnParams.add(new ScriptValue_Faux(this.getEnvironment(), ScriptValueType.DOUBLE));
 		this.addFauxFunction("setX", ScriptValueType.VOID, fxnParams, ScriptKeywordType.PUBLIC, false, false);
 		this.addFauxFunction("setY", ScriptValueType.VOID, fxnParams, ScriptKeywordType.PUBLIC, false, false);
 		this.addFauxFunction("setZ", ScriptValueType.VOID, fxnParams, ScriptKeywordType.PUBLIC, false, false);
 		assert Debugger.closeNode();
 	}
 
 	// Define default constructor here
 	@Override
 	public ScriptTemplate instantiateTemplate() {
 		return new FauxTemplate_Point(this.getEnvironment(), this.getType());
 	}
 
 	@Override
 	public void nodificate() {
 		assert Debugger.openNode("Point Faux Template");
 		super.nodificate();
 		assert Debugger.addNode(this.point);
 		assert Debugger.closeNode();
 	}
 
 	public void setPoint(Point point) {
 		this.point = point;
 	}
 }
