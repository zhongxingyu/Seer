 package codegen.C;
 
 import codegen.C.stm.T;
 import control.Control;
 
 public class PrettyPrintVisitor implements Visitor {
 	private int indentLevel;
 	private java.io.BufferedWriter writer;
 
 	public PrettyPrintVisitor() {
 		this.indentLevel = 0;
 	}
 
 	private void indent() {
 		this.indentLevel += 4;
 	}
 
 	private void unIndent() {
 		this.indentLevel -= 4;
 	}
 
 	private void printSpaces() {
 		int i = this.indentLevel;
 		while (i-- != 0)
 			this.say(" ");
 	}
 
 	private void sayln(String s) {
 		say(s);
 		try {
 			this.writer.write("\n");
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 	private void say(String s) {
 		try {
 			this.writer.write(s);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 	// /////////////////////////////////////////////////////
 	// expressions
 	@Override
 	public void visit(codegen.C.exp.Add e) {
 		this.say("(");
 		e.left.accept(this);
 		this.say(" + ");
 		e.right.accept(this);
 		this.say(")");
 	}
 
 	@Override
 	public void visit(codegen.C.exp.And e) {
 		this.say("(");
 		e.left.accept(this);
 		this.say(" && ");
 		e.right.accept(this);
 		this.say(")");
 	}
 
 	@Override
 	public void visit(codegen.C.exp.ArraySelect e) {
 		e.array.accept(this);
 		this.say("->__data[");
 		e.index.accept(this);
 		this.say("]");
 	}
 
 	@Override
 	public void visit(codegen.C.exp.Call e) {
 		this.say("(__gc_frame." + e.assign + "=");
 		e.exp.accept(this);
 		this.say(", ");
 		this.say("__gc_frame." + e.assign + "->vptr->" + e.id + "("
 				+ "__gc_frame." + e.assign);
 		int size = e.args.size();
 		if (size == 0) {
 			this.say("))");
 			return;
 		}
 		for (codegen.C.exp.T x : e.args) {
 			this.say(", ");
 			x.accept(this);
 		}
 		this.say("))");
 	}
 
 	@Override
 	public void visit(codegen.C.exp.Id e) {
 		if (e.isField)
 			this.say("this->");
 		else if (e.isLocal) {
 			if (e.type instanceof ast.type.IntArray
 					|| e.type instanceof ast.type.Class)
 				this.say("__gc_frame.");
 		}
 		this.say(e.id);
 	}
 
 	@Override
 	public void visit(codegen.C.exp.Length e) {
 		this.say("(");
 		e.array.accept(this);
 		this.say("->__u.length)");
 	}
 
 	@Override
 	public void visit(codegen.C.exp.Lt e) {
 		this.say("(");
 		e.left.accept(this);
 		this.say(" < ");
 		e.right.accept(this);
 		this.say(")");
 	}
 
 	@Override
 	public void visit(codegen.C.exp.NewIntArray e) {
 		this.say("Tiger_new_array(");
 		e.exp.accept(this);
 		this.say(")");
 	}
 
 	@Override
 	public void visit(codegen.C.exp.NewObject e) {
 		this.say("((struct " + e.id + "*)(Tiger_new(&" + e.id
 				+ "_vtable_, sizeof(struct " + e.id + "))))");
 	}
 
 	@Override
 	public void visit(codegen.C.exp.Not e) {
 		this.say("!");
 		this.say("(");
 		e.exp.accept(this);
 		this.say(")");
 	}
 
 	@Override
 	public void visit(codegen.C.exp.Num e) {
 		this.say(Integer.toString(e.num));
 	}
 
 	@Override
 	public void visit(codegen.C.exp.Sub e) {
 		this.say("(");
 		e.left.accept(this);
 		this.say(" - ");
 		e.right.accept(this);
 		this.say(")");
 	}
 
 	@Override
 	public void visit(codegen.C.exp.This e) {
 		this.say("this");
 	}
 
 	@Override
 	public void visit(codegen.C.exp.Times e) {
 		this.say("(");
 		e.left.accept(this);
 		this.say(" * ");
 		e.right.accept(this);
 		this.say(")");
 	}
 
 	// statements
 	@Override
 	public void visit(codegen.C.stm.Assign s) {
 		if (s.isField)
 			this.say("this->");
 		else if (s.isLocal) {
 			if (s.type instanceof ast.type.IntArray
 					|| s.type instanceof ast.type.Class)
 				this.say("__gc_frame.");
 		}
 		this.say(s.id + " = ");
 		s.exp.accept(this);
 		this.sayln(";");
 	}
 
 	@Override
 	public void visit(codegen.C.stm.AssignArray s) {
 		if (s.isField)
 			this.say("this->");
 		else if (s.isLocal)
 			this.say("__gc_frame.");
 		this.say(s.id + "->__data[");
 		s.index.accept(this);
 		this.say("] = ");
 		s.exp.accept(this);
 		this.sayln(";");
 	}
 
 	@Override
 	public void visit(codegen.C.stm.Block s) {
 		this.sayln("{");
 		this.indent();
 		for (T stm : s.stms) {
 			this.printSpaces();
 			stm.accept(this);
 		}
 		this.unIndent();
 		this.printSpaces();
 		this.sayln("}");
 	}
 
 	@Override
 	public void visit(codegen.C.stm.If s) {
 		this.say("if (");
 		s.condition.accept(this);
 		this.sayln(")");
 		this.indent();
 		this.printSpaces();
 		s.thenn.accept(this);
 		this.unIndent();
 		this.printSpaces();
 		this.sayln("else");
 		this.indent();
 		this.printSpaces();
 		s.elsee.accept(this);
 		this.sayln("");
 		this.unIndent();
 	}
 
 	@Override
 	public void visit(codegen.C.stm.Print s) {
 		this.say("System_out_println(");
 		s.exp.accept(this);
 		this.sayln(");");
 	}
 
 	@Override
 	public void visit(codegen.C.stm.While s) {
 		this.say("while (");
 		s.condition.accept(this);
 		this.sayln(")");
 		this.indent();
 		this.printSpaces();
 		s.body.accept(this);
 		this.unIndent();
 		this.sayln("");
 	}
 
 	// type
 	@Override
 	public void visit(codegen.C.type.Class t) {
 		this.say("struct " + t.id + " *");
 	}
 
 	@Override
 	public void visit(codegen.C.type.Int t) {
 		this.say("long");//because we need stack has same word size
 	}
 
 	@Override
 	public void visit(codegen.C.type.IntArray t) {
 		this.say("struct __tiger_obj_header *");
 	}
 
 	// dec
 	@Override
 	public void visit(codegen.C.dec.Dec d) {
 		d.type.accept(this);
 		this.say(" ");
 		this.sayln(d.id + ";");
 	}
 
 	// method
 	@Override
 	public void visit(codegen.C.method.Method m) {
 		m.retType.accept(this);
 		this.say(" " + m.classId + "_" + m.id + "(");
 		int size = m.formals.size();
 		for (codegen.C.dec.T d : m.formals) {
 			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
 			size--;
 			dec.type.accept(this);
 			this.say(" " + dec.id);
 			if (size > 0)
 				this.say(", ");
 		}
 		this.sayln(") {");
 		this.indent();
 
 		// generate gc-map for formals
 		this.printSpaces();
 		this.say("char *__arguments_gc_map = \"");
 		for (codegen.C.dec.T f : m.formals) {
 			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) f;
			if (dec.type instanceof codegen.C.type.Class)
 				this.say("1");
 			else
 				this.say("0");
 		}
 		this.sayln("\"; // generate gc-map for formals");
 
 		// generate gc-frame
 		this.sayln("");
 		this.printSpaces();
 		this.sayln("// START generate gc-frame");
 		this.printSpaces();
 		this.sayln("struct {");
 		this.indent();
 		this.printSpaces();
 		this.sayln("void *__prev;");
 		this.printSpaces();
 		this.sayln("char *__arguments_gc_map;");
 		this.printSpaces();
 		this.sayln("void *__arguments_base_address;");
 		this.printSpaces();
 		this.sayln("unsigned long __locals_gc_number;");
 		// method specified fields(locals) of reference type
 		for (codegen.C.dec.T d : m.locals) {
 			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
 			if (dec.type instanceof codegen.C.type.IntArray
 					|| dec.type instanceof codegen.C.type.Class) {
 				this.printSpaces();
 				dec.type.accept(this);
 				this.sayln(" " + dec.id + ";");
 			}
 		}
 		this.unIndent();
 		this.printSpaces();
 		this.sayln("} __gc_frame;");
 		this.printSpaces();
 		this.sayln("// END generate gc-frame\n");
 
 		// generate code to push gc-frame
 		this.printSpaces();
 		this.sayln("// START generate code to push gc-frame");
 		this.printSpaces();
 		this.sayln("memset(&__gc_frame, 0, sizeof(__gc_frame));//make sure reference init with NULL");
 		this.printSpaces();
 		this.sayln("__gc_frame.__prev = gc_frame_prev;");
 		this.printSpaces();
 		this.sayln("__gc_frame.__arguments_gc_map = __arguments_gc_map;");
 		this.printSpaces();
 		this.sayln("__gc_frame.__arguments_base_address = &this;");
 		this.printSpaces();
 		this.say("__gc_frame.__locals_gc_number = ");
 		int __locals_gc_number = 0;
 		for (codegen.C.dec.T f : m.locals) {
 			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) f;
 			if (dec.type instanceof codegen.C.type.Class
 					|| dec.type instanceof codegen.C.type.IntArray)
 				__locals_gc_number++;
 		}
 		this.say(new Integer(__locals_gc_number).toString());
 		this.sayln(";");
 		this.printSpaces();
 		this.sayln("gc_frame_prev = &__gc_frame;");
 		this.printSpaces();
 		this.sayln("// END generate code to push gc-frame\n");
 
 		// method specified fields(locals) of non-reference type
 		this.printSpaces();
 		this.sayln("// START method specified fields(locals) of non-reference type");
 		for (codegen.C.dec.T d : m.locals) {
 			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
 			if (dec.type instanceof codegen.C.type.Int) {
 				this.printSpaces();
 				dec.type.accept(this);
 				this.sayln(" " + dec.id + ";");
 			}
 		}
 		this.printSpaces();
 		this.sayln("// END method specified fields(locals) of non-reference type\n");
 
 		// method body
 		this.printSpaces();
 		this.sayln("// START real body");
 		for (codegen.C.stm.T s : m.stms) {
 			this.printSpaces();
 			s.accept(this);
 		}
 		this.printSpaces();
 		this.sayln("// END real body\n");
 
 		// generate code to pop gc-frame
 		this.printSpaces();
 		this.sayln("gc_frame_prev = __gc_frame.__prev; // pop gc-frame");
 
 		this.printSpaces();
 		this.say("return ");
 		m.retExp.accept(this);
 		this.sayln(";");
 		this.unIndent();
 		this.printSpaces();
 		this.sayln("}");
 	}
 
 	@Override
 	public void visit(codegen.C.mainMethod.MainMethod m) {
 		this.sayln("void Tiger_main (long __dummy) {");
 		this.sayln("//'__dummy' is just a dummy argument to get base address of argument in main");
 		this.indent();
 
 		// generate gc-frame
 		this.printSpaces();
 		this.sayln("// START generate gc-frame");
 		this.printSpaces();
 		this.sayln("struct {");
 		this.indent();
 		this.printSpaces();
 		this.sayln("void *__prev;");
 		this.printSpaces();
 		this.sayln("char *__arguments_gc_map;");
 		this.printSpaces();
 		this.sayln("void *__arguments_base_address;");
 		this.printSpaces();
 		this.sayln("unsigned long __locals_gc_number;");
 		// method specified fields(locals) of reference type
 		for (codegen.C.dec.T d : m.locals) {
 			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
 			if (dec.type instanceof codegen.C.type.IntArray
 					|| dec.type instanceof codegen.C.type.Class) {
 				this.printSpaces();
 				dec.type.accept(this);
 				this.sayln(" " + dec.id + ";");
 			}
 		}
 		this.unIndent();
 		this.printSpaces();
 		this.sayln("} __gc_frame;");
 		this.printSpaces();
 		this.sayln("// END generate gc-frame\n");
 
 		// generate code to push gc-frame
 		this.printSpaces();
 		this.sayln("// START generate code to push gc-frame");
 		this.printSpaces();
 		this.sayln("memset(&__gc_frame, 0, sizeof(__gc_frame));//make sure reference init with NULL");
 		this.printSpaces();
 		this.sayln("__gc_frame.__prev = gc_frame_prev;");
 		this.printSpaces();
 		this.sayln("__gc_frame.__arguments_gc_map = NULL;");
 		this.printSpaces();
 		this.sayln("__gc_frame.__arguments_base_address = &__dummy;");
 		this.printSpaces();
 		this.say("__gc_frame.__locals_gc_number = ");
 		int __locals_gc_number = 0;
 		for (codegen.C.dec.T f : m.locals) {
 			codegen.C.dec.Dec dec = (codegen.C.dec.Dec) f;
 			if (dec.type instanceof codegen.C.type.Class
 					|| dec.type instanceof codegen.C.type.IntArray)
 				__locals_gc_number++;
 		}
 		this.say(new Integer(__locals_gc_number).toString());
 		this.sayln(";");
 		this.printSpaces();
 		this.sayln("gc_frame_prev = &__gc_frame;");
 		this.printSpaces();
 		this.sayln("// END generate code to push gc-frame\n");
 
 		this.printSpaces();
 		this.sayln("// START real body");
 		this.printSpaces();
 		m.stm.accept(this);
 		this.printSpaces();
 		this.sayln("// END real body");
 
 		// generate code to pop gc-frame
 		this.printSpaces();
 		this.sayln("gc_frame_prev = __gc_frame.__prev; // pop gc-frame");
 
 		this.unIndent();
 		this.printSpaces();
 		this.sayln("}");
 	}
 
 	// vtables
 	@Override
 	public void visit(codegen.C.vtable.Vtable v) {
 		this.sayln("struct " + v.id + "_vtable {");
 		this.indent();
 		this.printSpaces();
 		this.sayln("const char *__class_gc_map;");
 		for (codegen.C.Ftuple t : v.ms) {
 			this.printSpaces();
 			t.ret.accept(this);
 			this.sayln(" (*" + t.id + ")();");
 		}
 		this.unIndent();
 		this.printSpaces();
 		this.sayln("};\n");
 	}
 
 	private void outputVtable(codegen.C.vtable.Vtable v) {
 		this.sayln("struct " + v.id + "_vtable " + v.id + "_vtable_ = {");
 		this.indent();
 
 		// generate class gc map
 		this.printSpaces();
 		this.say("\"");
 		codegen.C.classs.Class classs = (codegen.C.classs.Class)v.classs;
 		for (codegen.C.Tuple dec : classs.decs) {
 			if (dec.type instanceof codegen.C.type.IntArray ||
 					dec.type instanceof codegen.C.type.Class)
 				this.say("1");
 			else
 				this.say("0");
 		}
 		this.sayln("\",");
 
 
 		for (codegen.C.Ftuple t : v.ms) {
 			this.printSpaces();
 			this.sayln(t.classs + "_" + t.id + ",");
 		}
 		this.unIndent();
 		this.printSpaces();
 		this.sayln("};\n");
 	}
 
 	// class
 	@Override
 	public void visit(codegen.C.classs.Class c) {
 		this.sayln("struct " + c.id + " {");
 		this.indent();
 		this.printSpaces();
 		this.sayln("struct " + c.id + "_vtable *vptr;");
 		this.printSpaces();
 		this.sayln("int __obj_or_array;//0 for obj 1 for array");
 		this.printSpaces();
 		this.sayln("void *__forwarding;//used for gc");
 		for (codegen.C.Tuple t : c.decs) {
 			this.printSpaces();
 			t.type.accept(this);
 			this.say(" ");
 			this.sayln(t.id + ";");
 		}
 		this.unIndent();
 		this.printSpaces();
 		this.sayln("};");
 	}
 
 	// program
 	@Override
 	public void visit(codegen.C.program.Program p) {
 		// we'd like to output to a file, rather than the "stdout".
 		try {
 			String outputName = null;
 			if (Control.outputName != null)
 				outputName = Control.outputName;
 			else if (Control.fileName != null) {
 				int index = Control.fileName.indexOf("/");
 				String tmp = Control.fileName;
 				while (index != -1) {
 					tmp = tmp.substring(index + 1);
 					index = tmp.indexOf("/");
 				}
 				Control.outputName = outputName = "/tmp/" + tmp + ".c";
 			} else
 				Control.outputName = outputName = "/tmp/" + "a.c";
 
 			System.out.format("write output file to %s\n", Control.outputName);
 			this.writer = new java.io.BufferedWriter(
 					new java.io.OutputStreamWriter(
 							new java.io.FileOutputStream(outputName)));
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		this.sayln("// This is automatically generated by the Tiger compiler.");
 		this.sayln("// Do NOT modify!\n");
 		this.sayln("#include \"runtime.h\"\n");
 
 		this.sayln("\n// structures");
 		for (codegen.C.classs.T c : p.classes) {
 			c.accept(this);
 		}
 
 		this.sayln("\n// vtables structures");
 		for (codegen.C.vtable.T v : p.vtables) {
 			v.accept(this);
 		}
 		this.sayln("");
 
 		this.sayln("\n// declarations");
 		for (codegen.C.method.T generalM : p.methods) {
 			if (generalM instanceof codegen.C.method.Method) {
 				codegen.C.method.Method m = (codegen.C.method.Method) generalM;
 				m.retType.accept(this);
 				this.say(" " + m.classId + "_" + m.id + "(");
 				int size = m.formals.size();
 				for (codegen.C.dec.T d : m.formals) {
 					codegen.C.dec.Dec dec = (codegen.C.dec.Dec) d;
 					size--;
 					dec.type.accept(this);
 					this.say(" " + dec.id);
 					if (size > 0)
 						this.say(", ");
 				}
 				this.sayln(");");
 			} else {
 				/* couldn't happen */
 				System.err
 						.println("fatal error, method is not of codegen.C.method.Method class");
 				System.exit(3);
 			}
 		}
 		this.sayln("");
 
 		this.sayln("\n// vtables");
 		for (codegen.C.vtable.T v : p.vtables) {
 			outputVtable((codegen.C.vtable.Vtable) v);
 		}
 		this.sayln("");
 
 		this.sayln("\n// methods");
 		for (codegen.C.method.T m : p.methods) {
 			m.accept(this);
 		}
 		this.sayln("");
 
 		this.sayln("\n// main method");
 		p.mainMethod.accept(this);
 
 		try {
 			this.writer.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 }
