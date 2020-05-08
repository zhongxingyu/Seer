 ////////////////////////////////////////////////////////////////////////////////
 //
 //  ADOBE SYSTEMS INCORPORATED
 //  Copyright 2007 Adobe Systems Incorporated
 //  All Rights Reserved.
 //
 //  NOTICE: Adobe permits you to use, modify, and distribute this file
 //  in accordance with the terms of the license agreement accompanying it.
 //
 ////////////////////////////////////////////////////////////////////////////////
 
 package adobe.abc;
 
 import java.io.*;
 import java.util.*;
 
 import static macromedia.asc.embedding.avmplus.ActionBlockConstants.*;
 import static adobe.abc.ActionBlockConstants.*;
 import static java.lang.Boolean.TRUE;
 import static java.lang.Boolean.FALSE;
 
 /*
  * TODO
  * 
  * (done) merge phi nodes
  * (done) pretty print dags
  * (done) scope capture
  * (done) type tracking
  * (done) constness
  * (done) null tracking
  * (done) emitter
  * (done) convert lvn into dominator based dvn
  * (done) sort cpool entries by frequency of use
  * (done) eliminate unused cpool entries
  * (done) split dag nodes to tree nodes
  * (done) recalc local_count
  * (done) recalc max_stack
  * (done) recalc scope_depth
  * (done) allow nonempty stack between blocks - stk_sched2 
  * (done) give every slot a default value even if not from abc (vm-defined defaults)
  * (done) model exception edges, remove PURE
  * (done) flesh out branch simplifications in sccp
  * (done) respect try/catch regions
  * (done) don't assign local to OP_arg in catch.  use arg & xarg
  * (done) if N handlers specify the same catch block, ensure phi's merge args correctly.
  * (done) strip unnecessary slot default values on export
  * (done) preserve metadata
  * (done) expand getlex to expose redundant findproperty 
  * (done) add null-ness to sccp lattice, remove NOTNULL flag
  * (done) handle lt/gt correctly for strings
  * (done) hasnext2 codegen
  * (done) combine findpropstrict+getproperty into getlex when find's only use is get in same block.
  * (done) normalize inc/declocal/_i into inc/decrement/_i
  * 
  * force stkout contents for xarg blocks rather than clearing stack at xarg definition.
  * don't allow xarg block to be combined with other blocks.
  * handle OP_equals for all const types
  * istype optimizations
  * model external data dependencies, remove EFFECT
  * treat pushwith & pushscope appropriately in findprop
  * expand implicit conversions to expose them to optimizations
  * fold implicit conversions to reduce code size
  * use locals for activation slots that dont escape (need array ssa?)
  * algebraic identities
  * combine sccp values & types into single lattice
  * add final-ness to Typeref
  * interprocedural analysis to determine effect of functions
  * check (prove?) correctness of SKIPTEST
  * insert kills & coerces to appease AVM verifier
  * add a no-catch edge to an EXIT block for any block with a PX expr.
  * if we have catch blocks, any PX should end its block.  (FCFG notation is premature)
  * preds should include all edges, not just normal edges
  * script loading dependencies.  remove EFFECT from findprop/finddef if script is already loaded.
  * convert chain of ifstricne<int> into switch if ints are dense
  * write a verifier (easier than testing with actual avm)
  * merge scripts based on init dependencies.
  * in scheduler, propagate live & stk across exception edges
  * get smarter about removing phi(coerce) without losing verifiability
  * scope stack scheduling
  * turn add into increment/_i when possible
  * turn increment/decrement/_i into inc/declocal/_i if in/out assigned to same local in sched.
  * inline method if target is known and body is smaller than call (eg isNaN, trivial getters)
  * 
  * TODO algorithms
  * 
  * partial redundancy elimination
  * alias analysis
  * tail merging
  * loop inversion
  * remove redundant path expressions
  *
  * TODO file format changes
  * 
  * remove/shorten version number
  * short branch offsets
  * optional init methods
  * every reference is a relative offset (eliminate lookup tables)
  * remove empty activation traits
  * optional trait names
  * OP_add_d
  * OP_concat
  */
  
 /**
  * @author Edwin Smith
  */
 public class GlobalOptimizer
 {
 	// default configuration flags
 	final boolean PRESERVE_METHOD_NAMES = false;
 	final boolean USE_CALLMETHOD = false;
 	final boolean OUTPUT_DOT = false;
 	final boolean SHOW_DFG =  false;
 	final boolean SHOW_CODE = false;
 	boolean STRIP_DEBUG_INFO = true;
 	boolean ALLOW_NATIVE_CTORS = false;	// not final, AbcThunkGen needs to set this to true
 	
 	boolean verbose_mode=false;
 	boolean legacy_verifier=false;  
 
 	public static void main(String[] args) throws IOException
 	{
 		GlobalOptimizer go = new GlobalOptimizer(); 
  		List<InputAbc> a = new ArrayList<InputAbc>();
  		List<Integer> lengths = new ArrayList<Integer>();
 		String filename = null;
 		byte[] before = null;
 		//  Index of first export in the file list.
 		int first_exported_file = 0;
  		boolean obscure_natives = false; 
  		boolean no_c_gen = false; 
 		boolean quiet_mode = false;
 
 		for (int i = 0; i < args.length; ++i)
 		{
  			if(args[i].equals("-obscure_natives")) {
 				obscure_natives = true;
  				continue;
  			}
  			if(args[i].equals("-no_c_gen")) {
 				no_c_gen = true;
  				continue;
  			}
 			if(args[i].equals("-d")) {
 				go.STRIP_DEBUG_INFO=false;
 				continue;
 			}
 			if(args[i].equals("-verbose")) {
 				go.verbose_mode = true;
 				quiet_mode = false;
 				continue;
 			}
 			if ( args[i].equals("-quiet"))
 			{
 				quiet_mode = true;
 				go.verbose_mode = false;
 				continue;
 			}
 			if ( args[i].equals("-legacy_verifier"))
 			{
 				go.legacy_verifier = !go.legacy_verifier;
 				go.verboseStatus("legacy_verifier set to " + go.legacy_verifier);
 				continue;
 			}
 			if(args[i].equals("--")) {
 				first_exported_file = a.size();
 				continue;
             }
 
 			filename = args[i];
 			before = load(filename);
 			lengths.add(before.length);
 			InputAbc ia = go.new InputAbc();
 			ia.readAbc(before);
 			a.add(ia);
 			if(obscure_natives) {
 				ia.obscure_natives();
 				obscure_natives = false;
 			}
 		}
 
 		if ( 0 == args.length || first_exported_file >= a.size() )
 		{
 			System.err.println("usage: GlobalOptimizer [-obscure_natives] [-no_c_gen] [-verbose] [-quiet] [imports] -- [exports]");
 			return;
 		}
 		
 		// merge exports together
 		// TODO this is not right, it only works for builtin ABCs and only under TT...
 		// we really need to construct a new script (that calls all the scripts in the initScripts list)
 		// and append it to the end as the new initscript.
 		List<Integer> initScripts = new ArrayList<Integer>();
 		InputAbc first = a.get(first_exported_file);
  		int before_length = lengths.get(first_exported_file);
 		initScripts.add(first.scripts.length - 1);
 
 		//  Combine other exported files.
 		for(int i=first_exported_file+1; i < a.size(); i++) {
 			first.combine(a.get(i));
 			initScripts.add(first.scripts.length - 1);
 			before_length += lengths.get(i);
 		}
 		
 		// Optimize the combined ABC file and emit.
 		go.optimize(first);
 		byte[] after = go.emit(first, filename, initScripts, no_c_gen);
 		
 		//  Print summary statistics.
 		if ( ! quiet_mode )
 		{
 			System.out.println();
 			System.out.println("Before optimization: "+before_length);
 			System.out.println("After optimization:  "+after.length);
 			int delta = before_length - after.length;
 			long percent = Math.round(delta/(double)before_length*100);
 			System.out.println("Saved:  "+delta+ " "+percent+"%");
 		}
 	}
 
 	static byte[] load(String filename) throws IOException
 	{
 		InputStream in = new FileInputStream(filename);
 		try
 		{
 			byte[] before = new byte[in.available()];
 			in.read(before);
 			return before;
 		}
 		finally
 		{
 			in.close();
 		}
 	}
 
 	class InputAbc
 	{
 		String strings[];
 		int ints[];
 		long uints[];
 		double doubles[];
 		Namespace namespaces[];
 		Nsset nssets[];
 		Name names[];
 		Method methods[];
 		Metadata metadata[];
 		Type classes[];
 		Type scripts[];
 		boolean containsObject;
 		Set<Type> toResolve;
 		List<InputAbc> mergedAbcs = new ArrayList<InputAbc>();
 		
 		InputAbc()
 		{
 			mergedAbcs.add(this);
 		}
 		
 		Type lookup(String name, Type base)
 		{
 			Name n = new Name(name);
 			Type t = namedTypes.get(n);
 			if (t == null)
 			{
 				Name n2 = new Name(CONSTANT_Qname, new Namespace(CONSTANT_PackageNamespace, ""), name);
 				t = namedTypes.get(n2);
 				if (t == null)
 				{
 					t = new Type(n2, base);
 					namedTypes.put(n2, t);
 				}
 			}
 			return t;
 		}
 		
 		Type lookup(String name)
 		{
 			return lookup(name,OBJECT);
 		}
 
 		void lookupBuiltins()
 		{
 			assert(containsObject);
 
 			CLASS = lookup("Class");
 			FUNCTION = lookup("Function");
 			ARRAY = lookup("Array");
 			INT = lookup("int");
 			UINT = lookup("uint");
 			NUMBER = lookup("Number");
 			BOOLEAN = lookup("Boolean");
 			STRING = lookup("String");
 			NAMESPACE = lookup("Namespace");
 			XML = lookup("XML");
 			XMLLIST = lookup("XMLList");
 			QNAME = lookup("QName");
 			VOID = lookup("void",null);
 			
 			OBJECT.ctype = CTYPE_ATOM;
 			NULL.ctype = CTYPE_ATOM;
 			ANY.ctype = CTYPE_ATOM;
 			VOID.ctype = CTYPE_VOID;
 			INT.ctype = CTYPE_INT;
 			UINT.ctype = CTYPE_UINT;
 			NUMBER.ctype = CTYPE_DOUBLE;
 			BOOLEAN.ctype = CTYPE_BOOLEAN;
 			STRING.ctype = CTYPE_STRING;
 			NAMESPACE.ctype = CTYPE_NAMESPACE;
 			// everything else defaults to CTYPE_OBJECT
 
 			INT.numeric = NUMBER.numeric = UINT.numeric = BOOLEAN.numeric = true;
 			
 			STRING.primitive = BOOLEAN.primitive = true;
 			INT.primitive = NUMBER.primitive = UINT.primitive = true;
 			VOID.primitive = NULL.primitive = true;
 			
 			ANY.atom = OBJECT.atom = VOID.atom = true;
 			
 			INT.ref = INT.ref.nonnull();
 			NUMBER.ref = NUMBER.ref.nonnull();
 			UINT.ref = UINT.ref.nonnull();
 			BOOLEAN.ref = BOOLEAN.ref.nonnull();
 			
 			OBJECT.defaultValue = NULL;
 			NULL.defaultValue = NULL;
 			ANY.defaultValue = UNDEFINED;
 			VOID.defaultValue = UNDEFINED;
 			BOOLEAN.defaultValue = FALSE;
 			NUMBER.defaultValue = Double.NaN;
 			INT.defaultValue = 0;
 			UINT.defaultValue = 0;
 		}
 		
 		Type lookup(int id)
 		{
 			if (id == 0)
 				return ANY;
 			if (!namedTypes.contains(names[id]))
 			{
 				// external reference.  create placeholder type.
 				Name n = new Name(CONSTANT_Qname, names[id].nsset(0), names[id].name);
 				namedTypes.put(n, new Type(n,null));
 			}
 			return namedTypes.get(names[id]);
 		}
 
 		void readAbc(byte[] abc) throws IOException
 		{
 			Reader p = new Reader(0, abc);
 			if (p.readU16() != 16 || p.readU16() != 46)
 				throw new RuntimeException("not an abc file");
 		
 			ints = new int[p.readU30() + 1];
 			for (int i = 1, n = ints.length - 1; i < n; i++)
 				ints[i] = p.readU30();
 		
 			uints = new long[p.readU30() + 1];
 			for (int i = 1, n = uints.length - 1; i < n; i++)
 				uints[i] = 0xffffffffL & p.readU30();
 		
 			doubles = new double[p.readU30() + 1];
 			for (int i = 1, n = doubles.length - 1; i < n; i++)
 				doubles[i] = p.readDouble();
 		
 			strings = new String[p.readU30() + 1];
 			strings[0] = "";
 			for (int i = 1, n = strings.length - 1; i < n; i++)
 			{
 				int len = p.readU30();
 				strings[i] = new String(abc, p.pos, len, "UTF-8");
 				p.pos += len;
 			}
 		
 			namespaces = new Namespace[p.readU30() + 1];
 			// TODO this should be AnyNamespace, not PUBLIC
 			namespaces[0] = PUBLIC;
 			for (int i = 1, n = namespaces.length - 1; i < n; i++)
 				namespaces[i] = new Namespace(p.readU8(), strings[p.readU30()]);
 		
 			nssets = new Nsset[p.readU30() + 1];
 			for (int i = 1, n = nssets.length-1; i < n; i++)
 			{
 				nssets[i] = new Nsset(new Namespace[p.readU30()]);
 				for (int j = 0, m = nssets[i].length; j < m; j++)
 					nssets[i].nsset[j] = namespaces[p.readU30()]; // ns_index
 			}
 		
 			names = new Name[p.readU30() + 1];
 			for (int i = 1, n = names.length-1; i < n; i++)
 				names[i] = readName(p);
 		
 			methods = new Method[p.readU30()];
 			int[] methodpos = new int[methods.length];
 			for (int i = 0, n = methods.length; i < n; i++)
 				methods[i] = readMethod(p, methodpos, i);
 		
 			metadata = new Metadata[p.readU30()];
 			for (int i = 0, n = metadata.length; i < n; i++)
 				metadata[i] = readMetadata(p);
 		
 			toResolve = new HashSet<Type>();
 		
 			Type[] instances = new Type[p.readU30()];
 			for (int i = 0, n = instances.length; i < n; i++)
 				instances[i] = readInstance(p); 
 		
 			if (containsObject)
 				lookupBuiltins();
 		
 			// finish resolving methods using mp we saved earlier
 			for (int i=0, n = methods.length; i < n; i++)
 				resolveSignatureType(new Reader(methodpos[i], p.abc), i, methods[i]);
 			
 			classes = new Type[instances.length];
 			for (int i = 0, n = classes.length; i < n; i++)
 				classes[i] = readClass(p, instances[i]);
 			
 			scripts = new Type[p.readU30()];
 			for (int i = 0, n = scripts.length; i < n; i++)
 				scripts[i] = readScript(p, i);
 
 			for (int i = 0, n = p.readU30(); i < n; i++)
 				readBody(p);
 			
 			// lazy resolve slot type names
 			for (Type t: toResolve)
 				resolveType(t);
 			
 			toResolve = null;
 		}
 		
 		void obscure_natives()
 		{
 			for(Type t: classes) {
 				t.obscure_natives = true;
 				if (t.itype != null)
 					t.itype.obscure_natives = true;
 			}
 			for(Type t: scripts) {
 				t.obscure_natives = true;
 				if (t.itype != null)
 					t.itype.obscure_natives = true;
 			}
 		}
 		
 		void combine(InputAbc abc)
 		{
 			int[] newInts = new int[ints.length + abc.ints.length];
 			System.arraycopy(ints, 0, newInts, 0, ints.length);
 			System.arraycopy(abc.ints, 0, newInts, ints.length, abc.ints.length);
 			ints = newInts;
 
 			long[] newUints = new long[uints.length + abc.uints.length];
 			System.arraycopy(uints, 0, newUints, 0, uints.length);
 			System.arraycopy(abc.uints, 0, newUints, uints.length, abc.uints.length);
 			uints = newUints;
 
 			double[] newDoubles = new double[doubles.length + abc.doubles.length];
 			System.arraycopy(doubles, 0, newDoubles, 0, doubles.length);
 			System.arraycopy(abc.doubles, 0, newDoubles, doubles.length, abc.doubles.length);
 			doubles = newDoubles;		
 			
 			String[] newStrings = new String[strings.length + abc.strings.length];
 			System.arraycopy(strings, 0, newStrings, 0, strings.length);
 			System.arraycopy(abc.strings, 0, newStrings, strings.length, abc.strings.length);
 			strings = newStrings;
 			
 			Namespace[] newNamespaces = new Namespace[namespaces.length + abc.namespaces.length];
 			System.arraycopy(namespaces, 0, newNamespaces, 0, namespaces.length);
 			System.arraycopy(abc.namespaces, 0, newNamespaces, namespaces.length, abc.namespaces.length);
 			namespaces = newNamespaces;
 			
 			Nsset[] newNssets = new Nsset[nssets.length + abc.nssets.length];
 			System.arraycopy(nssets, 0, newNssets, 0, nssets.length);
 			System.arraycopy(abc.nssets, 0, newNssets, nssets.length, abc.nssets.length);
 			nssets = newNssets;
 			
 			Name[] newNames = new Name[names.length + abc.names.length];
 			System.arraycopy(names, 0, newNames, 0, names.length);
 			System.arraycopy(abc.names, 0, newNames, names.length, abc.names.length);
 			names = newNames;
 			
 			Method[] newMethods = new Method[methods.length + abc.methods.length];
 			System.arraycopy(methods, 0, newMethods, 0, methods.length);
 			System.arraycopy(abc.methods, 0, newMethods, methods.length, abc.methods.length);
 			methods = newMethods;
 			
 			// we change the .abc property for Methods but not Bindings, canEarlyBind* 
 			// deals with this by checking that Binding.abc is in the Method.abc.mergedAbcs set
 			for(Method m: abc.methods) {
 				m.abc = this;
 			}
 
 			Metadata[] newMetadata = new Metadata[metadata.length + abc.metadata.length];
 			System.arraycopy(metadata, 0, newMetadata, 0, metadata.length);
 			System.arraycopy(abc.metadata, 0, newMetadata, metadata.length, abc.metadata.length);
 			metadata = newMetadata;
 
 			Type[] newClasses = new Type[classes.length + abc.classes.length];
 			System.arraycopy(classes, 0, newClasses, 0, classes.length);
 			System.arraycopy(abc.classes, 0, newClasses, classes.length, abc.classes.length);
 			classes = newClasses;
 			
 			Type[] newScripts = new Type[scripts.length + abc.scripts.length];
 			System.arraycopy(scripts, 0, newScripts, 0, scripts.length);
 			System.arraycopy(abc.scripts, 0, newScripts, scripts.length, abc.scripts.length);
 			scripts = newScripts;
 			
 			mergedAbcs.add(abc);
 		}
 		
 		void resolveType(Type t)
 		{
 			if (t.size != 0)
 				return;
 			int size = 0;
 			if (t.base != null)
 			{
 				resolveType(t.base);
 				size = t.base.size;
 			}
 			int hole = -1;
 			for (Binding b: t.defs.values())
 			{
 				if (isSlot(b))
 				{
 					if (!isClass(b))
 						resolveSlotType(b);
 					// NOTE NOTE NOTE NOTE NOTE
 					// NOTE NOTE NOTE NOTE NOTE
 					// NOTE NOTE NOTE NOTE NOTE
 					// NOTE NOTE NOTE NOTE NOTE
 					//
 					// the logic here is replicated in QVM and GlobalOptimizer.java and is assumed by the slot offsets
 					// that it generates for native classes. If you change this here you must also change it there.
 					//
 					// NOTE NOTE NOTE NOTE NOTE
 					// NOTE NOTE NOTE NOTE NOTE
 					// NOTE NOTE NOTE NOTE NOTE
 					// NOTE NOTE NOTE NOTE NOTE
 					// NOTE NOTE NOTE NOTE NOTE
 					//
 					// @todo, this generates slots that are correct for 32-bit but not 64 bit builds
 					//
 					if (b.type.t == NUMBER)
 					{
 						if (size%8 != 0)
 						{
 							hole = size;
 							size += 4;
 						}
 						b.offset = size;
 						size += 8;
 					}
 					else
 					{
 						if (hole != -1)
 						{
 							b.offset = hole;
 							hole = -1;
 						}
 						else
 						{
 							b.offset = size;
 							size += 4;
 						}
 					}
 				}
 			}
 			if (size > 0)
 				verboseStatus("sizeof "+t+" "+size);
 			t.size = size;
 		}
 
 		Metadata readMetadata(Reader p)
 		{
 			Metadata md = new Metadata();
 			md.name = strings[p.readU30()];
 			Attr[] attrs = md.attrs = new Attr[p.readU30()];
 			for (int j = 0, n=attrs.length; j < n; j++)
 				attrs[j] = new Attr(strings[p.readU30()]);
 			for (int j = 0, n=attrs.length; j < n; j++)
 				attrs[j].value = strings[p.readU30()];
 			return md;
 		}
 		
 		void resolveSignatureType(Reader p, int i, Method m)
 		{
 			m.returns = lookup(p.readU30()).ref; // return type
 			for (int j = 1; j < m.params.length; j++)
 				m.params[j] = lookup(p.readU30()).ref; // param type
 
 			p.readU30(); // debug name
 			p.readU8(); // flags
 			if (m.hasOptional())
 			{
 				int optional_count = p.readU30();
 				m.values = new Object[m.params.length];
 				int param_count = m.params.length - 1;
 				for (int j = param_count - optional_count+1; j <= param_count; j++)
 				{
 					m.values[j] = readArgDefault(p);
 					assert(m.values[j] != null);
 				}
 			}
 		}
 
 		void readBody(Reader p)
 		{
 			Method m = methods[p.readU30()];
 			m.max_stack = p.readU30();
 			m.local_count = p.readU30();
 			m.max_scope = -(p.readU30()-p.readU30());
 			m.code_len = p.readU30();
 			Reader pcode = new Reader(p);
 			p.pos += m.code_len;
 			readCode(m, pcode, p);
 			Type act = new Type();
 			m.activation = act.ref.nonnull();
 			act.base = ANY;
 			if (m.name != null)
 				act.name = m.name.append(" activation");
 			readTraits(p, act);
 		}
 
 		Method readMethod(Reader p, int[] methodpos, int i)
 		{
 			Method m = new Method(i, this);
 			int param_count = p.readU30();
 			m.params = new Typeref[param_count+1];
 			m.params[0] = ANY.ref;
 			methodpos[i] = p.pos;
 			p.readU30(); // return type
 			for (int j = 1; j <= param_count; j++)
 				p.readU30(); // param type
 			m.debugName = new Name(strings[p.readU30()]); // debug name
 			m.name = m.debugName;
 			m.flags = p.readU8();
 			if (m.hasOptional())
 			{
 				int optional_count = p.readU30();
 				assert(optional_count > 0);
 				while (optional_count-- > 0)
 					readArgDefault(p);
 				m.values = new Object[m.params.length];
 				for (int j = param_count - optional_count+1; j <= param_count; j++)
 					m.values[j] = readArgDefault(p);
 			}
 			if (m.hasParamNames())
 			{
 				m.paramNames = new Name[param_count+1];
 				for (int j = 1; j <= param_count; j++)
 					m.paramNames[j] = new Name(strings[p.readU30()]);
 			}
 			return m;
 		}
 		
 		Name readName(Reader p)
 		{
 			int kind = p.readU8();
 			switch (kind)
 			{
 			default:
 				throw new RuntimeException("Unknown name kind: "+kind);
 			case CONSTANT_TypeName:
 			{
 				int index = p.readU30();	// Index to the Multiname type, i.e. Vector
 				int count = p.readU30();	// number of type parameter names
 				assert(count == 1);			// all we support for now
 				int typeparm = p.readU30();	// Multinames for the type parameters, i.e. String for a Vector.<String>
 				Name mn = this.names[index];
 				return new Name(kind, mn.name, mn.nsset, typeparm);
 			}
 			case CONSTANT_Qname:
 			case CONSTANT_QnameA:
 				return new Name(kind, namespaces[p.readU30()], strings[p.readU30()]);
 			case CONSTANT_Multiname:
 			case CONSTANT_MultinameA:
 				return new Name(kind, strings[p.readU30()], nssets[p.readU30()]);
 			case CONSTANT_RTQname:
 			case CONSTANT_RTQnameA:
 				return new Name(kind, uniqueNs(), strings[p.readU30()]);
 			case CONSTANT_MultinameL:
 			case CONSTANT_MultinameLA:
 				return new Name(kind, unique(), nssets[p.readU30()]);
 			case CONSTANT_RTQnameL:
 			case CONSTANT_RTQnameLA:
 				return new Name(kind);
 			}
 		}
 		
 		void readTraits(Reader p, Type t)
 		{
 			toResolve.add(t);
 			t.defs = new Symtab<Binding>();
 			int slot_id = 0;
 			if (t.base != null)
 				slot_id = t.base.slotCount;
 			int count = p.readU30();
 			for (int i = 0; i < count; i++)
 			{
 				Name name = names[p.readU30()]; // name
 				Binding b = new Binding(p.readU8(), name, this);
 				Binding old = t.defs.get(name);
 				while (old != null && old.peer != null)
 					old = old.peer;
 				if (old != null)
 					old.peer = b;
 				t.defs.put(name, b);
 				int slot = p.readU30(); // slot_id | disp_id
 				b.id = p.readU30(); // typename | class_id | method_id
 				switch (b.kind())
 				{
 				case TRAIT_Var:
 				case TRAIT_Const:
 				case TRAIT_Class:
 					if (isClass(b))
 					{
 						// TODO this is a bit tenuous
 						b.type = classes[b.id].ref.nonnull();
 						b.value = NULL;
 					}
 					else
 					{
 						b.value = readSlotDefault(p);
 						b.type = ANY.ref;
 						if (b.value instanceof Namespace)
 							namespaceNames.put((Namespace)b.value, name);
 					}
 
 					// TODO add vm's logic to accept or ignore compiler-assigned slot_id
 					if (slot == 0)
 						slot = ++slot_id;
 					else if (slot > slot_id)
 						slot_id = slot;
 					b.slot = slot;
 					break;
 				case TRAIT_Method:
 				case TRAIT_Getter:
 				case TRAIT_Setter:
 					b.slot = 0;
 					Method m = b.method = methods[b.id];
 					m.cx = t;
 					m.params[0] = t.ref.nonnull();
 					m.kind = isMethod(b) ? "function" : isGetter(b) ? "get" : "set";
 					m.name = name;
 					// ignore compiler-assigned disp_id's
 					// if (!slot) slot = ++disp_id;
 					// else if (slot > disp_id) disp_id = slot;
 					break;
 				default:
 					System.err.println("illegal trait kind " + b.kind() + " at offset " + p.pos);
 					assert (false);
 				}
 
 				if (b.hasMetadata())
 				{
 					b.md = new Metadata[p.readU30()];
 					for (int j=0, m=b.md.length; j < m; j++)
 						b.md[j] = metadata[p.readU30()];
 				}
 			}
 			t.slotCount = slot_id;
 		}
 		
 		Object readSlotDefault(Reader p)
 		{
 			int i = p.readU30();
 			if (i != 0)
 			{
 				int kind = p.readU8();
 				return defaultValue(kind,i);
 			}
 			return null;
 		}
 		
 		Object readArgDefault(Reader p)
 		{
 			int i = p.readU30();
 			int kind = p.readU8();
 			Object v = defaultValue(kind, i);
 			return v;
 		}
 		
 		Object defaultValue(int kind, int i)
 		{
 			switch (kind)
 			{
 			case CONSTANT_False:
 				return FALSE;
 			case CONSTANT_True:
 				return TRUE;
 			case CONSTANT_Null:
 				return NULL;
 			case CONSTANT_Void:
 				return UNDEFINED;
 			case CONSTANT_Utf8:
 				return strings[i];
 			case CONSTANT_Integer:
 				return ints[i];
 			case CONSTANT_UInteger:
 				return uints[i];
 			case CONSTANT_Double:
 				return doubles[i];
 			case CONSTANT_Namespace:
 			case CONSTANT_PackageNamespace:
 			case CONSTANT_ProtectedNamespace:
 			case CONSTANT_PackageInternalNs:
 			case CONSTANT_ExplicitNamespace:
 			case CONSTANT_StaticProtectedNs:
 			case CONSTANT_PrivateNamespace:
 				return namespaces[i];
 			}
 			assert(false);
 			return null;
 		}
 
 		void resolveSlotType(Binding b)
 		{
 			b.type = lookup(b.id).ref;
 			if (b.value == null)
 				b.value = b.type.t.defaultValue;
 		}
 
 		Type readScript(Reader p, int i)
 		{
 			Type s = new Type();
 			assert(OBJECT != null);
 			s.base = OBJECT;
 			s.name = new Name("global"+i);
 			Method init = s.init = methods[p.readU30()]; // init_id
 			init.cx = s;
 			Typeref sref = s.ref.nonnull();
 			init.params[0] = sref;
 			init.name = s.name;
 			init.kind = "init";
 			readTraits(p, s);
 			for (Binding b : s.defs.values())
 				globals.put(b.name, sref);
 			s.setFinal();
 			return s;
 		}
 
 		Type readClass(Reader p, Type it)
 		{
 			Type c = new Type();
 			assert(CLASS != null);
 			c.base = CLASS;
 			c.itype = it;
 			c.name = it.name.append("$");
 			Method init = c.init = methods[p.readU30()]; // init_id
 			init.cx = c;
 			init.params[0] = c.ref.nonnull();
 			init.name = c.name;
 			init.kind = "init";
 			readTraits(p, c);
 			c.setFinal();
 			return c;
 		}
 
 		Type readInstance(Reader p)
 		{
 			Type t = new Type();
 			t.name = names[p.readU30()]; // name (qname)
 			t.base = lookup(p.readU30()); // base (multiname)
 			t.flags = p.readU8();
 			if (t.hasProtectedNs())
 				t.protectedNs = namespaces[p.readU30()];
 			t.interfaces = new Type[p.readU30()];
 			for (int j = 0, n = t.interfaces.length; j < n; j++)
 				t.interfaces[j] = lookup(p.readU30()); // interface
 			t.init = methods[p.readU30()]; // init_id
 			if(t.init.isNative() && !ALLOW_NATIVE_CTORS)
 				throw new RuntimeException("Constructors can't be native: "+t);
 			t.init.cx = t;
 			t.init.params[0] = t.ref.nonnull();
 			t.init.kind = "init";
 			t.init.name = t.name;
 			readTraits(p, t);
 			namedTypes.put(t.name, t);
 			
 			if (t.name.equals(new Name(PKG_PUBLIC,"Object")))
 			{
 				containsObject = true;
 				OBJECT = t;
 				NULL = lookup("null", OBJECT);
 			}
 			return t;
 		}
 
 		void readCode(Method m, Reader p, Reader ptry)
 		{
 			int local_count = m.local_count;
 			int end_pos = p.pos + m.code_len;
 			
 			// initial state of frame
 			Expr[] frame = new Expr[local_count + m.max_scope + m.max_stack];
 			Map<Integer,Block> blocks = new TreeMap<Integer,Block>();
 			Map<Block,FrameState> states = new TreeMap<Block,FrameState>();
 			int scopep = local_count;
 			int sp = local_count+m.max_scope;
 			
 			m.entry = new Edge(m,null,0);
 			Block b = createBlock(m, m.entry, states, frame, sp, scopep);
 			Expr e;
 			
 			{
 				int i;
 				for (i=0; i < m.params.length; i++)
 				{
 					// argument i
 					b.add(e = frame[i] = new Expr(m, OP_arg, i));
 					if (i==0)
 						e.ref = new Name("this");
 					else if (m.paramNames != null)
 						e.ref = m.paramNames[i];
 					else 
 						e.ref = new Name("arg"+i);
 				}
 				
 				if (m.needsArguments() || m.needsRest())
 				{
 					b.add(e = frame[i] = new Expr(m, OP_arg, i));
 					e.ref = new Name(m.needsArguments() ? "arguments" : "rest");
 					i++;
 				}
 				
 				for (; i < local_count; i++)
 				{
 					// remaining locals are undefined, but we use OP_arg
 					// to suppress any codegen.  sccp_eval knows what to do.
 					b.add(e = frame[i] = new Expr(m, OP_arg, i));
 					e.ref = new Name("local"+i);
 				}
 			}
 			
 			BitSet trylabels = new BitSet();
 			BitSet catchlabels = new BitSet();
 			int code_start = p.pos;
 			int try_start = ptry.pos;
 			
 			Handler[] handlers = m.handlers = new Handler[ptry.readU30()];
 			for (int j = 0, n=handlers.length; j < n; j++)
 			{
 				Handler h = handlers[j] = new Handler();
 				
 				int from = ptry.readU30();
 				int to = ptry.readU30();
 				int target = ptry.readU30();
 				h.type = lookup(ptry.readU30()).ref.nonnull();
 				Name name = h.name = names[ptry.readU30()];
 				Type a = new Type(name,ANY);
 				h.activation = a.ref.nonnull();
 				Binding bind = new Binding(TRAIT_Var, name, this);
 				bind.type = h.type;
 				a.defs.put(name, bind);
 
 				trylabels.set(from);
 				trylabels.set(to);
 				catchlabels.set(target);
 			}
 			
 			boolean reachable = true;
 			while (p.pos < end_pos)
 			{
 				int pos = p.pos;
 				int op = p.readU8();
 
 				if (op == OP_label || blocks.containsKey(p.pos-1) ||
 						trylabels.get(pos-code_start) ||
 						catchlabels.get(pos-code_start))
 				{
 					Edge edge = null;
 					if (reachable)
 					{
 						assert(!catchlabels.get(pos-code_start));
 						Edge succ[] = b.succ();
 						if (succ == null)
 						{
 							b.add(e = new Expr(m,OP_jump));
 							e.succ = new Edge[] { edge = new Edge(m,b,0,blocks.get(pos)) };
 						}
 						else
 						{
 							edge = succ[0];
 						}
 					}
 					else if (catchlabels.get(pos-code_start))
 					{
 						scopep = local_count;
 						sp = local_count + m.max_scope + 1;
 					}
 					merge(m,edge, blocks, states, pos, frame, sp, scopep);
 					b = blocks.get(pos);
 					FrameState state = states.get(b);
 					System.arraycopy(state.frame, 0, frame, 0, frame.length);
 					sp = state.sp;
 					scopep = state.scopep;
 					reachable = true;
 				}
 				
 				// now we know what block we're in and pos is where it starts.
 				if (handlers.length > 0)
 				{
 					if (b.xsucc == noedges)
 					{
 						// start of new block.
 						List<Edge> xsucc = new ArrayList<Edge>();
 						ptry.pos = try_start;
 						for (int j=0,n=ptry.readU30(); j < n; j++)
 						{
 							int from = code_start + ptry.readU30();
 							int to = code_start + ptry.readU30();
 							int target = code_start + ptry.readU30();
 							if (pos >= from && pos < to)
 							{
 								Edge edge = new Edge(m, b, j, handlers[j]);
 								xmerge(m, edge, blocks, states, target, frame, sp, scopep);
 								xsucc.add(edge);
 							}
 							ptry.readU30(); // type
 							ptry.readU30(); // name
 						}
 						b.xsucc = xsucc.toArray(new Edge[xsucc.size()]);
 					}
 				}
 				
 				switch (op)
 				{
 				case OP_label:
 					// already handled above
 					break;
 
 				case OP_throw:
 				case OP_returnvalue:
 					b.add(e = new Expr(m,op, frame, sp--, 1));
 					e.succ = noedges;
 					reachable = false;
 					merge(m,null, blocks, states, p.pos, frame, sp, scopep);
 					break;
 					
 				case OP_dxnslate:
 					b.add(new Expr(m,op,frame, sp--, 1));
 					break;
 
 				case OP_pushwith:
 				case OP_pushscope:
 				{
 					// includes a null pointer check and moves value to scope stack, so this isn't just a copy
 					b.add(frame[scopep++] = e = new Expr(m, op, frame, sp--, 1));
 					break;
 				}
 
 				case OP_popscope:
 					b.add(e = new Expr(m, op));
 					e.scopes = new Expr[] { frame[--scopep] };
 					frame[scopep] = null;
 					break;
 					
 				case OP_nextname:
 				case OP_hasnext:
 				case OP_nextvalue:
 					b.add(e = new Expr(m,op, frame, sp, 2));
 					sp -= 2;
 					frame[sp++] = e;
 					break;
 					
 				case OP_pushnull:
 					b.add(frame[sp++] = new Expr(m,op,NULL));
 					break;
 				case OP_pushundefined:
 					b.add(frame[sp++] = new Expr(m,op,UNDEFINED));
 					break;
 				case OP_pushtrue:
 					b.add(frame[sp++] = new Expr(m,op,TRUE));
 					break;
 				case OP_pushfalse:
 					b.add(frame[sp++] = new Expr(m,op,FALSE));
 					break;
 				case OP_pushnan:
 					b.add(frame[sp++] = new Expr(m,op,Double.NaN));
 					break;
 					
 				case OP_newactivation:
 					b.add(frame[sp++] = new Expr(m,op));
 					break;
 
 				case OP_getglobalscope:
 					b.add(e = frame[sp++] = new Expr(m, op));
 					e.scopes = capture(frame, scopep, scopep-local_count);
 					break;
 					
 				case OP_getscopeobject:
 					b.add(e = frame[sp++] = new Expr(m, op));
 					e.scopes = capture(frame, local_count+p.readU8()+1, 1);
 					break;
 					
 				case OP_pop:
 					sp--;
 					break;
 
 				case OP_dup:
 					frame[sp] = frame[sp-1];
 					sp++;
 					break;
 					
 				case OP_swap:
 				{
 					e = frame[sp-1];
 					frame[sp-1] = frame[sp-2];
 					frame[sp-2] = e;
 					break;
 				}
 				
 				case OP_returnvoid:
 					b.add(e = new Expr(m,op));
 					e.succ = noedges;
 					reachable = false;
 					merge(m,null, blocks, states, p.pos, frame, sp, scopep);
 					break;
 
 				case OP_convert_s:
 				case OP_esc_xelem:
 				case OP_esc_xattr:
 				case OP_convert_i:
 				case OP_convert_u:
 				case OP_convert_d:
 				case OP_coerce_s:
 				case OP_negate:
 				case OP_increment:
 				case OP_decrement:
 				case OP_not:
 				case OP_bitnot:
 				case OP_increment_i:
 				case OP_decrement_i:
 				case OP_negate_i:
 				case OP_coerce_o:
 				case OP_convert_o:
 				case OP_typeof:
 				case OP_convert_b:
 				case OP_coerce_a:
 					// unary w/ possible side effect
 					b.add(frame[sp-1] = new Expr(m,op, frame, sp, 1));
 					break;
 
 				case OP_checkfilter:
 					// possible exception
 					b.add(new Expr(m,op, frame, sp, 1));
 					break;
 
 				case OP_astypelate:
 				case OP_add:
 				case OP_subtract:
 				case OP_multiply:
 				case OP_divide:
 				case OP_modulo:
 				case OP_lshift:
 				case OP_rshift:
 				case OP_urshift:
 				case OP_bitand:
 				case OP_bitor:
 				case OP_bitxor:
 				case OP_equals:
 				case OP_lessthan:
 				case OP_lessequals:
 				case OP_greaterthan:
 				case OP_greaterequals:
 				case OP_instanceof:
 				case OP_in:
 				case OP_add_i:
 				case OP_subtract_i:
 				case OP_multiply_i:
 				case OP_istypelate:
 				case OP_strictequals:
 					b.add(frame[sp-2] = new Expr(m,op, frame, sp, 2));
 					sp--;
 					break;
 					
 				case OP_getlocal0:
 				case OP_getlocal1:
 				case OP_getlocal2:
 				case OP_getlocal3:
 					frame[sp++] = frame[op-OP_getlocal0];
 					break;
 
 				case OP_setlocal0:
 				case OP_setlocal1:
 				case OP_setlocal2:
 				case OP_setlocal3:
 					frame[op-OP_setlocal0] = frame[--sp];
 					break;
 
 				case OP_kill:
 					b.add(frame[p.readU30()] = new Expr(m,OP_pushundefined,UNDEFINED)); 
 					break;
 					
 				case OP_pushshort:
 					b.add(frame[sp++] = new Expr(m,op, new Integer((short)p.readU30())));
 					break;
 				case OP_pushstring:
 					b.add(frame[sp++] = new Expr(m,op, strings[p.readU30()]));
 					break;
 				case OP_pushint:
 					b.add(frame[sp++] = new Expr(m,op, new Integer(ints[p.readU30()])));
 					break;
 				case OP_pushuint:
 					b.add(frame[sp++] = new Expr(m,op, new Long(uints[p.readU30()])));
 					break;
 				case OP_pushdouble:
 					b.add(frame[sp++] = new Expr(m,op, new Double(doubles[p.readU30()])));
 					break;
 				case OP_pushnamespace:
 					b.add(frame[sp++] = new Expr(m,op, namespaces[p.readU30()]));
 					break;
 
 				case OP_getlocal:
 					frame[sp++] = frame[p.readU30()];
 					break;
 					
 				case OP_setlocal:
 					frame[p.readU30()] = frame[--sp];
 					break;
 					
 				case OP_coerce:
 				case OP_astype:
 				case OP_istype:
 					b.add(e = frame[sp-1] = new Expr(m, op, names[p.readU30()], frame, sp, 1));
 					break;
 					
 				case OP_dxns:
 					b.add(new Expr(m,op, strings[p.readU30()]));
 					break;
 				
 				case OP_inclocal_i:
 				case OP_declocal_i:
 				case OP_inclocal:
 				case OP_declocal:
 				{
 					// turn these into increment/decrement, forget which local was used.
 					int i = p.readU30();
 					op = op == OP_inclocal_i ? OP_increment_i :
 						 op == OP_inclocal   ? OP_increment   :
 						 op == OP_declocal_i ? OP_decrement_i : 
 					 	                       OP_decrement;
 					b.add(e = frame[i] = new Expr(m, op, i, frame, i+1, 1));
 					break;
 				}
 
 				case OP_newfunction:
 				{
 					e = new Expr(m,op);
 					e.m = methods[p.readU30()];
 					e.scopes = capture(frame, scopep, scopep-local_count);
 					b.add(frame[sp++] = e);
 					break;
 				}
 				
 				case OP_newclass:
 				{
 					e = new Expr(m, op, frame, sp, 1);
 					e.scopes = capture(frame, scopep, scopep-local_count);
 					e.c = classes[p.readU30()];
 					b.add(frame[sp-1] = e);
 					break;
 				}
 				
 				case OP_newobject:
 				{
 					int argc = 2*p.readU30();
 					e = new Expr(m,op, frame, sp, argc);
 					sp -= argc;
 					b.add(frame[sp++] = e);
 					break;
 				}
 				
 				case OP_newarray:
 				{
 					int argc = p.readU30();
 					e = new Expr(m,op, frame, sp, argc);
 					sp -= argc;
 					b.add(frame[sp++] = e);
 					break;
 				}
 
 				case OP_newcatch:
 				{
 					e = new Expr(m, op, p.readU30());
 					b.add(frame[sp++] = e);
 					break;
 				}
 				
 				case OP_getsuper:
 				{
 					Name ref = names[p.readU30()];
 					int argc = 1 + refArgc[ref.kind];
 					e = new Expr(m,op, ref, frame, sp, argc);
 					sp -= argc;
 					b.add(frame[sp++] = e);
 					break;
 				}
 				
 				case OP_setsuper:
 				{
 					Name ref = names[p.readU30()];
 					int argc = 2 + refArgc[ref.kind];
 					b.add(e = new Expr(m,op, ref, frame, sp, argc));
 					sp -= argc;
 					break;
 				}
 				
 				case OP_call:
 				{
 					int argc = 2+p.readU30();
 					e = new Expr(m, op, frame, sp, argc);
 					sp -= argc;
 					b.add(frame[sp++] = e);
 					break;
 				}
 
 				case OP_construct:
 				{
 					int argc = 1+p.readU30();
 					e = new Expr(m,op, frame, sp, argc);
 					sp -= argc;
 					b.add(frame[sp++] = e);
 					break;
 				}
 
 				case OP_getdescendants:
 				case OP_deldescendants:
 				case OP_deleteproperty:
 				case OP_getproperty:
 				{
 					Name ref = names[p.readU30()];
 					int argc = 1 + refArgc[ref.kind];
 					e = new Expr(m, op, ref, frame, sp, argc);
 					sp -= argc;
 					b.add(frame[sp++] = e);
 					break;
 				}
 				
 				case OP_getlex:
 				{
 					Name ref = names[p.readU30()];
 					assert(refArgc[ref.kind] == 0);
 					e = new Expr(m, OP_findpropstrict, ref, frame, sp, 0);
 					e.scopes = capture(frame, scopep, scopep-local_count);
 					b.add(e);
 					e = new Expr(m, OP_getproperty, ref, new Expr[] { e }, 1, 1);
 					b.add(frame[sp++] = e);
 					break;
 				}
 
 				case OP_findpropstrict:
 				case OP_findproperty:
 				{
 					Name ref = names[p.readU30()];
 					int argc = refArgc[ref.kind];
 					e = new Expr(m,op, ref, frame, sp, argc);
 					e.scopes = capture(frame, scopep, scopep-local_count);
 					sp -= argc;
 					b.add(frame[sp++] = e);
 					break;
 				}
 
 				case OP_finddef:
 				{
 					Name ref = names[p.readU30()];
 					int argc = refArgc[ref.kind];
 					e = new Expr(m,op, ref, frame, sp, argc);
 					sp -= argc;
 					b.add(frame[sp++] = e);
 					break;
 				}
 					
 				case OP_setproperty:
 				case OP_initproperty:
 				{
 					Name ref = names[p.readU30()];
 					int argc = 2 + refArgc[ref.kind];
 					e = new Expr(m,op, ref, frame, sp, argc);
 					sp -= argc;
 					b.add(e);
 					break;
 				}
 				
 				case OP_getslot:
 				{
 					e = new Expr(m,op, p.readU30(), frame, sp, 1);
 					b.add(e);
 					frame[sp-1] = e;
 					break;
 				}
 				
 				case OP_setslot:
 				{
 					e = new Expr(m,op, p.readU30(), frame, sp, 2);
 					b.add(e);
 					sp -= 2;
 					break;
 				}
 				
 				case OP_jump:
 				{
 					int offset = p.readS24();
 					if (reachable)
 					{
 						b.add(e = new Expr(m,op));
 						e.succ = new Edge[] { new Edge(m,b, 0, blocks.get(p.pos+offset)) };
 						merge(m, null, blocks, states, p.pos, frame, sp, scopep);
 						merge(m,e.succ[0], blocks, states, p.pos+offset, frame, sp, scopep);
 						reachable = false;
 					}
 					break;
 				}
 
 				case OP_iftrue:
 				case OP_iffalse:
 				{
 					b.add(e = new Expr(m, op, frame, sp--, 1));
 					int offset = p.readS24();
 					e.succ = new Edge[] { 
 							new Edge(m,b,0,blocks.get(p.pos)), 
 							new Edge(m,b,1,blocks.get(p.pos+offset)) };
 					merge(m,null, blocks, states, p.pos, frame, sp, scopep);
 					merge(m,e.succ[1], blocks, states, p.pos+offset, frame, sp, scopep);
 					break;
 				}
 				
 				case OP_ifnlt:
 				case OP_ifnle:
 				case OP_ifngt:
 				case OP_ifnge:
 				case OP_ifne:
 				case OP_ifstrictne:
 				{
 					b.add(e = new Expr(m, ifoper(op), frame, sp, 2));
 					b.add(e = new Expr(m, OP_iffalse, new Expr[] { e }, 1, 1));
 					int offset = p.readS24();
 					sp-=2;
 					e.succ = new Edge[] { 
 							new Edge(m,b,0,blocks.get(p.pos)),
 							new Edge(m,b,1,blocks.get(p.pos+offset)) };
 					merge(m,null, blocks, states, p.pos, frame, sp, scopep);
 					merge(m,e.succ[1], blocks, states, p.pos+offset, frame, sp, scopep);
 					break;
 				}
 
 				case OP_ifeq:
 				case OP_iflt:
 				case OP_ifle:
 				case OP_ifgt:
 				case OP_ifge:
 				case OP_ifstricteq:
 				{
 					b.add(e = new Expr(m, ifoper(op), frame, sp, 2));
 					b.add(e = new Expr(m, OP_iftrue, new Expr[] { e }, 1, 1));
 					int offset = p.readS24();
 					sp-=2;
 					e.succ = new Edge[] { 
 							new Edge(m,b,0,blocks.get(p.pos)),
 							new Edge(m,b,1,blocks.get(p.pos+offset)) };
 					merge(m,null, blocks, states, p.pos, frame, sp, scopep);
 					merge(m,e.succ[1], blocks, states, p.pos+offset, frame, sp, scopep);
 					break;
 				}
 					
 				case OP_lookupswitch:
 				{
 					b.add(e = new Expr(m,op, frame,sp--,1));
 					int target = pos+p.readS24();
 					int n = 1+p.readU30();
 					e.succ = new Edge[n+1];
 					e.succ[n] = new Edge(m,b, n, blocks.get(target));
 					merge(m,e.succ[n], blocks, states, target, frame, sp, scopep);
 					for (int i=0; i < n; i++)
 					{
 						target = pos+p.readS24();
 						e.succ[i] = new Edge(m,b, i, blocks.get(target));
 						merge(m,e.succ[i], blocks, states, target, frame, sp, scopep);
 					}
 					
 					// no fall-through for switch
 					reachable = false;
 					break;
 				}
 
 				case OP_pushbyte:
 				{
 					e = frame[sp++] = new Expr(m,op, new Integer((byte)p.readU8()));
 					b.add(e);
 					break;
 				}
 				
 				case OP_hasnext2:
 				{
 					int oloc = p.readU30();
 					int iloc = p.readU30();
 					Expr index = frame[iloc];
 					Expr obj = frame[oloc];
 					b.add(e = frame[sp] = new Expr(m,op));
 					e.locals = new Expr[] { obj, index };
 					b.add(e = frame[oloc] = new Expr(m, OP_hasnext2_o));
 					e.locals = new Expr[] { obj };
 					b.add(e = frame[iloc] = new Expr(m, OP_hasnext2_i));
 					e.locals = new Expr[] { index };
 					sp++;
 					break;
 				}
 				
 				case OP_callmethod:
 				{
 					int id = p.readU30();
 					int argc = 1+p.readU30();
 					e = new Expr(m,op, id, frame, sp, argc);
 					sp -= argc;
 					b.add(frame[sp++] = e);
 					break;
 				}
 				
 				case OP_callstatic:
 				{
 					int id = p.readU30();
 					int argc = 1+p.readU30();
 					e = new Expr(m,op, frame, sp, argc);
 					e.m = methods[id];
 					sp -= argc;
 					b.add(frame[sp++] = e);
 					break;
 				}
 				
 				case OP_applytype:
 				{
 					int argc = 1+p.readU30();
 					e = new Expr(m,op, frame, sp, argc);
 					sp -= argc;
 					b.add(frame[sp++] = e);
 					break;
 				}
 				
 				case OP_constructsuper:
 				{
 					int argc = 1+p.readU30();
 					e = new Expr(m,op, frame, sp, argc);
 					sp -= argc;
 					b.add(e);
 					break;
 				}
 				
 				case OP_callsuper:
 				case OP_callproperty:
 				case OP_constructprop:
 				case OP_callproplex:
 				{
 					Name ref = names[p.readU30()];
 					int argc = 1 + p.readU30() + refArgc[ref.kind];
 					e = new Expr(m,op, ref, frame, sp, argc);
 					sp -= argc;
 					b.add(frame[sp++] = e);
 					break;
 				}
 				
 				case OP_callsupervoid:
 				case OP_callpropvoid:
 				{
 					Name ref = names[p.readU30()];
 					int argc = 1 + p.readU30() + refArgc[ref.kind];
 					e = new Expr(m,op, ref, frame, sp, argc);
 					sp -= argc;
 					b.add(e);
 					break;
 				}
 				
 				case OP_debugfile:
 				{
 					int ii = p.readU30();
 					if (!STRIP_DEBUG_INFO)
 						b.add(e = new Expr(m,op, strings[ii]));
 					break;
 				}	
 
 				case OP_debugline:
 				case OP_bkptline:
 				{
 					int ii = p.readU30();
 					if (!STRIP_DEBUG_INFO)
 						b.add(e = new Expr(m,op, ii));
 					break;
 				}
 				case OP_debug:
 				{
 					int i1 = p.readU8();
 					int i2 = p.readU30();
 					int i3 = p.readU8();
 					int i4 = p.readU30();
 					if (!STRIP_DEBUG_INFO)
 					{
 						e = new Expr(m,op);
 						e.imm = new int[] { i1, i2, i3, i4 };
 						b.add(e);
 					}
 					break;
 				}
 
 				case OP_bkpt:
 					if (!STRIP_DEBUG_INFO)
 					{
 						// no args but position matters
 						b.add(new Expr(m,op));
 					}
 					break;
 
 				case OP_timestamp:
 					// no args but position matters
 					b.add(new Expr(m,op));
 					break;
 					
 				case OP_nop:
 					break;
 					
 				default:
 					System.err.println("Unknown ABC bytecode "+op);
 					assert (false);
 				}
 			}
 			
 			dce(m);
 		}
 		
 	}
 	
 	// vm-wide lookup tables
 	Symtab<Type> namedTypes = new Symtab<Type>();
 	Symtab<Typeref> globals = new Symtab<Typeref>();
 	Map<Namespace,Name> namespaceNames = new HashMap<Namespace,Name>();
 	
 	static class Handler
 	{
 		Typeref type;
 		Block entry;
 		Typeref activation;
 		Name name;
 		
 		public String toString()
 		{
 			return "catch "+type;
 		}
 	}
 	
 	static final Handler[] nohandlers = new Handler[] {};
 	
 	static class Method implements Comparable<Method>
 	{
 		InputAbc abc;
 		final int id;
 		int emit_id;
 		Edge entry;
 		Typeref[] params;
 		Object[] values;
 		Typeref returns;
 		Name name;
 		Name debugName;
 		Name[] paramNames;
 		int flags;
 		Type cx;
 		int blockId;
 		int exprId;
 		int edgeId;
 		String kind;
 		
 		// body fields
 		int max_stack;
 		int local_count;
 		int max_scope;
 		int code_len;
 		
 		Typeref activation;
 		Handler[] handlers = nohandlers;
 		
 		Method(int id, InputAbc abc)
 		{
 			this.id = id;
 			this.abc = abc;
 		}
 		boolean needsRest()
 		{
 			return (flags & METHOD_Needrest) != 0;
 		}
 		boolean needsArguments()
 		{
 			return (flags & METHOD_Arguments) != 0;
 		}
 		boolean hasParamNames()
 		{
 			return (flags & METHOD_HasParamNames) != 0;
 		}
 		boolean hasOptional()
 		{
 			return (flags & METHOD_HasOptional) != 0;
 		}
 		boolean isNative()
 		{
 			return (flags & METHOD_Native) != 0;
 		}
 		
 		public int compareTo(Method m)
 		{
 			return id - m.id;
 		}
 		
 		public String toString()
 		{
 			return kind + " " + String.valueOf(name);
 		}
 	}
 	
 	static class Binding
 	{
 		final InputAbc abc;
 		final Name name;
 		final private int flags_kind;
 		int slot;
 		int id;
 		int offset; // if slot
 		
 		// var, const, class
 		Object value;// default value if any
 		Typeref type; // if slot
 		
 		// function, getter, setter, method
 		Method method;	
 		
 		// metadata
 		Metadata[] md = nometadata;
 		
 		// if this is a get/set, points to corresponding set/get, if any.
 		Binding peer = null;
 
 		Binding(int kind, Name name, InputAbc abc)
 		{
 			this.name = name;
 			this.flags_kind = kind;
 			this.abc = abc;
 		}
 		
 		int kind()
 		{
 			return flags_kind & 15;
 		}
 		
 		boolean isFinal()
 		{
 			return ((flags_kind>>4) & TRAIT_FLAG_final) != 0;
 		}
 
 		boolean isOverride()
 		{
 			return ((flags_kind>>4) & TRAIT_FLAG_override) != 0;
 		}
 		
 		boolean hasMetadata()
 		{
 			return ((flags_kind>>4) & TRAIT_FLAG_metadata) != 0;
 		}
 		
 		public String toString()
 		{
 			switch (kind())
 			{
 			case TRAIT_Class: return "["+slot+"] class";
 			case TRAIT_Var: return "["+slot+"] var";
 			case TRAIT_Const: return "["+slot+"] const";
 			case TRAIT_Method: return "["+slot+"] method";
 			case TRAIT_Getter: return "["+slot+"] get";
 			case TRAIT_Setter: return "["+slot+"] set";
 			default:
 				assert(false);
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * compare two names.  this implements the multiname->name matching rules.
 	 * 
 	 * @param other
 	 * @return
 	 */
 	static public int match(Name a, Name b)
 	{
 		if (a == b) return 0;
 		
 		// @names can't ever match regular names
 		int d;
 		if ((d = a.attr() - b.attr()) != 0) return d;
 		if ((d = a.name.compareTo(b.name)) != 0) return d;
 		
 		if (a.isQname() && b.isQname())
 			return a.nsset(0).compareTo(b.nsset(0));
 		
 		else if (b.isQname() && !b.isQname())
 		{
 			for (Namespace ns: b.nsset)
 				if (ns.equals(a.nsset(0)))
 					return 0;
 		}
 		else if (b.isQname() && !a.isQname())
 		{
 			for (Namespace ns: a.nsset)
 				if (ns.equals(b.nsset(0)))
 					return 0;
 		}
 
 		return a.nsset.compareTo(b.nsset);
 	}
 	
 	static class Symtab<E>
 	{
 		private List<Name> names = new ArrayList<Name>();
 		private List<E> values = new ArrayList<E>();
 		
 		E get(Name name)
 		{
 			if (name.nsset != null && name.name != null)
 			{
 				if (name.nsset.length == 1)
 				{
 					for (int i=0, n=names.size(); i < n; i++)
 						if (0 == match(name, names.get(i)))
 							return values.get(i);
 				}
 				else
 				{
 					for (Namespace ns : name.nsset)
 					{
 						E e = get(new Name(name.kind,ns,name.name));
 						if (e != null)
 							return e;
 					}
 				}
 			}
 			return null;
 		}
 		
 		Name getName(Name n)
 		{
 			// return the matching name from the symbol table
 			if (n.nsset.length > 1)
 			{
 				for (Namespace ns : n.nsset)
 				{
 					Name k = new Name(n.kind,ns,n.name);
 					if (names.contains(k))
 						return k;
 				}
 			}
 			return n;
 		}
 		
 		boolean contains(Name n)
 		{
 			return n != null && get(n) != null;
 		}
 		
 		void put(Name n, E e)
 		{
 			assert(n.nsset.length == 1);
 			names.add(n);
 			values.add(e);
 		}
 		
 		public String toString()
 		{
 			StringBuilder b = new StringBuilder();
 			b.append('[');
 			for (int i=0, n=size(); i < n; i++)
 			{
 				b.append(names.get(i)).append('=').append(values.get(i));
 				if (i+1 < n)
 					b.append(", ");
 			}
 			b.append(']');
 			return b.toString();
 		}
 		
 		public Collection<E> values()
 		{
 			return values;
 		}
 		
 		int size()
 		{
 			return names.size();
 		}
 	}	
 	
 	static class Namespace implements Comparable<Namespace>
 	{
 		final int kind;
 		final String uri;
 		private final String comparableUri;
 		
 		Namespace(String uri)
 		{
 			this(CONSTANT_Namespace, uri);
 		}
 		
 		Namespace(int kind, String uri)
 		{
 			this.kind = kind;
 			this.uri = uri;
 			this.comparableUri = isPrivate() ? unique() : uri;
 		}
 		
 		boolean isPublic()
 		{
 			return (kind == CONSTANT_Namespace || kind == CONSTANT_PackageNamespace) && "".equals(uri);
 		}
 		
 		boolean isInternal()
 		{
 			return kind == CONSTANT_PackageInternalNs;
 		}
 		
 		boolean isPrivate()
 		{
 			return kind == CONSTANT_PrivateNamespace;
 		}
 		
 		boolean isPrivateOrInternal()
 		{
 			return isPrivate() || isInternal();
 		}
 
 		boolean isProtected()
 		{
 			return kind == CONSTANT_ProtectedNamespace ||
 				   kind == CONSTANT_StaticProtectedNs;
 		}
 		
 		public String toString()
 		{
 			return uri.length() > 0 ? uri : "public";
 		}
 		
 		public int hashCode()
 		{
 			return kind ^ uri.hashCode();
 		}
 		
 		public boolean equals(Object o)
 		{
 			if (!(o instanceof Namespace))
 				return false;
 			Namespace other = (Namespace) o;
 			return kind == other.kind && comparableUri.equals(comparableUri);
 		}
 
 		public int compareTo(Namespace other)
 		{
 			if (other == null) return 1; // nonnull > null
 			int i;
 			if ((i = kind-other.kind) != 0) return i;
 			return comparableUri.compareTo(other.comparableUri);
 		}
 	}
 	
 	static int rtcounter;
 	static String unique()
 	{
 		return unique("[]");
 	}
 	static String unique(String prefix)
 	{
 		return prefix+(rtcounter++);
 	}
 	static Namespace uniqueNs()
 	{
 		return new Namespace(unique("ns"));
 	}
 
 	static class Nsset implements Comparable<Nsset>, Iterable<Namespace>
 	{
 		final Namespace[] nsset;
 		final int length;
 		Nsset(Namespace[] nsset)
 		{
 			this.nsset = nsset;
 			this.length = nsset.length;
 		}
 		
 		public int hashCode()
 		{
 			int h = deepHashCode(nsset);
 			return h;
 		}
 		
 		public boolean equals(Object other)
 		{
 			if (!(other instanceof Nsset)) return false;
 			Namespace[] s2 = ((Nsset)other).nsset;
 			Namespace[] s1 = nsset;
 			return deepEquals(s1, s2);
 		}
 		
 		public int compareTo(Nsset other)
 		{
 			Namespace[] s1 = nsset;
 			Namespace[] s2 = other.nsset;
 			int d;
 			if ((d = s1.length - s2.length) != 0) return d;
 			for (int i=0, n=s1.length; i < n; i++)
 				if ((d = s1[i].compareTo(s2[i])) != 0) return d;
 			return 0;
 		}
 
 		public Iterator<Namespace> iterator()
 		{
 			return Arrays.asList(nsset).iterator();
 		}
 		
 		public String toString()
 		{
 			return Arrays.toString(nsset);
 		}
 	}
 	
     public static int deepHashCode(Object a[]) 
     {
         if (a == null)
             return 0;
  
         int result = 1;
  
         for (Object element : a) {
             int elementHash = 0;
             if (element != null)
                 elementHash = element.hashCode();
             result = 31 * result + elementHash;
         }
  
         return result;
     }
 	
     public static boolean deepEquals(Object[] a1, Object[] a2) 
     {
         if (a1 == a2)
             return true;
         if (a1 == null || a2==null)
             return false;
         int length = a1.length;
         if (a2.length != length)
             return false;
  
         for (int i = 0; i < length; i++) {
             Object e1 = a1[i];
             Object e2 = a2[i];
  
             if (e1 == e2)
                 continue;
             if (e1 == null)
                 return false;
  
             // Figure out whether the two elements are equal
             if (!e1.equals(e2))
                 return false;
         }
         return true;
     }
 	
 	static class Name implements Comparable<Name>
 	{
 		final int kind;
 		private final Nsset nsset;
 		final String name;
 		final int type_param;	// 0 if none
 
 		Name(int kind)
 		{
 			this(kind, uniqueNs(), unique());
 		}
 		Name(Namespace ns, String name)
 		{
 			this(CONSTANT_Qname, ns, name);
 		}
 		Name(int kind, Namespace ns, String name)
 		{
 			this(kind, name, new Nsset(new Namespace[] { ns }), 0);
 		}
 		Name(int kind, String name, Nsset nsset)
 		{
 			this(kind, name, nsset, 0);
 		}
 		Name(int kind, String name, Nsset nsset, int type_param)
 		{
 			assert(nsset != null);
 			this.kind = kind;
 			this.nsset = nsset;
 			this.name = name;
 			this.type_param = type_param;
 		}
 		Name(String name)
 		{
 			this(CONSTANT_Qname, PUBLIC, name);
 		}
 		
 		Namespace nsset(int i)
 		{
 			return nsset.nsset[i];
 		}
 		
 		public String toString()
 		{
 			return name;
 		}
 		
 		public String format()
 		{
 			if (nsset.length == 1)
 				return nsset(0) + "::" + name;
 			else
 			{
 				ArrayList<Namespace> list = new ArrayList<Namespace>();
 				for (Namespace n : nsset)
 					list.add(n);
 				return list + "::" + name;
 			}
 		}
 		
 		public Name append(String s)
 		{
 			return new Name(kind, name+s, nsset);
 		}
 
 		public Name prepend(String s)
 		{
 			return new Name(kind, s+name, nsset);
 		}
 		
 		int hc(Object o)
 		{
 			return o != null ? o.hashCode() : 0;
 		}
 		
 		public int hashCode()
 		{
 			return kind ^ hc(nsset) ^ hc(name);
 		}
 		
 		/**
 		 * exact equality.  Both names must have the same kind, name,
 		 * and equal namespace sets.
 		 */
 		public boolean equals(Object other)
 		{
 			if (!(other instanceof Name))
 				return false;
 			Name o = (Name) other;
 			return kind == o.kind && name.equals(o.name) && nsset.equals(o.nsset);
 		}
 		
 		public int compareTo(Name other)
 		{
 			int d = kind - other.kind;
 			if (d != 0) return d;
 			d = name.compareTo(other.name);
 			if (d != 0) return d;
 			return nsset.compareTo(other.nsset);
 		}
 		
 		int attr()
 		{
 			return kind == CONSTANT_MultinameA || kind == CONSTANT_QnameA ||
 				kind == CONSTANT_RTQnameA || kind == CONSTANT_RTQnameLA || kind == CONSTANT_MultinameLA ? 1 : 0;
 		}
 		
 		boolean isQname()
 		{
 			return kind == CONSTANT_Qname || kind == CONSTANT_QnameA ||
 				kind == CONSTANT_RTQname || kind == CONSTANT_RTQnameA ||
 				kind == CONSTANT_RTQnameL || kind == CONSTANT_RTQnameLA;
 		}
 	}
 	
 	static final Object UNDEFINED = new Object() { public String toString() { return "undefined"; }};
 	static final Object BOTTOM = new Object() { public String toString() { return "?"; }};
 	static final Double NAN = Double.NaN;
 	Type ANY = new Type(new Name("*"),null);
 	static Namespace PUBLIC = new Namespace("");
 	static Namespace PKG_PUBLIC = new Namespace(CONSTANT_PackageNamespace, "");
 	static Namespace AS3 = new Namespace("http://adobe.com/AS3/2006/builtin");
 	static Name AS3_TOSTRING = new Name(AS3, "toString");
 	
 	Type OBJECT, FUNCTION, CLASS, ARRAY;
 	Type INT, UINT, NUMBER, BOOLEAN, STRING, NAMESPACE;
 	Type XML, XMLLIST, QNAME;
 	Type NULL, VOID;
 	
 	static class Metadata implements Comparable
 	{
 		String name;
 		Attr[] attrs;
 		
 		public int compareTo(Object o)
 		{
 			Metadata md = (Metadata)o;
 			if (this == md) return 0;
 			int d = md.name.compareTo(name);
 			if (d != 0) return d;
 			if ((d = attrs.length - md.attrs.length) != 0) return d;
 			for (int i=0, n=attrs.length; i < n; i++)
 				if ((d = attrs[i].compareTo(md.attrs[i])) != 0)
 					return d;
 			return 0;
 		}
 	}
 
 	static class Attr implements Comparable
 	{
 		String name;
 		String value;
 		Attr(String name)
 		{
 			this.name = name;
 		}
 		
 		public int compareTo(Object o)
 		{
 			Attr a = (Attr)o;
 			if (this == a) return 0;
 			int d = name.compareTo(a.name);
 			if (d != 0) return d;
 			if ((d = value.compareTo(a.value)) != 0) return d;
 			return 0;
 		}
 	}
 	
 	static final Metadata[] nometadata = new Metadata[0];
 
 	/**
 	 * Methods ready for optimization.
 	 * Populated by readyMethod(), called from readyType()
 	 * and by initial processing of the OP_newfunction Expr.
 	 */
 	List<Method> ready = new ArrayList<Method>();
 
 	/** 
 	 * Track already-processed methods so they never go on the ready list twice.
 	 */
 	Set<Method> already_processed = new TreeSet<Method>();
 	
 	void readyType(Type t)
 	{
 		ready.add(t.init);
 		for (Binding b1: t.defs.values())
 			if (b1.method != null)
 				readyMethod(b1.method);
 	}
 	
 	void readyMethod(Method m)
 	{
		if (m.entry != null && ! already_processed.contains(m))
 		{
 			ready.add(m);
 			already_processed.add(m);
 		}
 	}
 	
 	void optimize(InputAbc a)
 	{
  		for (Type t: a.scripts)
 			readyType(t);
 		
 		while (!ready.isEmpty())
 			optimize(remove(ready));
 	}
 	
 	static class Ranker<T> implements Comparable
 	{
 		T value;
 		int rank;
 		Ranker(T value, int rank)
 		{
 			this.value = value;
 			this.rank = rank;
 		}
 		public int compareTo(Object o)
 		{
 			return ((Ranker)o).rank - rank;
 		}
 	}
 	
 	static class Pool<T extends Comparable>
 	{
 		Map<T,Integer> refs = new HashMap<T,Integer>();
 		ArrayList<T> values;
 		int countFrom;
 		
 		Pool(int countFrom)
 		{
 			this.countFrom = countFrom;
 		}
 		
 		int add(T e)
 		{
 			int n = !refs.containsKey(e) ? 1 : refs.get(e) + 1;
 			refs.put(e, n);
 			return n;
 		}
 		
 		@SuppressWarnings("unchecked")
 		void sort()
 		{
 			Ranker<T>[] arr = new Ranker[refs.size()];
 			int i=0;
 			for (T e: refs.keySet())
 				arr[i++] = new Ranker<T>(e,refs.get(e));
 			assert(i==refs.size());
 			Arrays.sort(arr);
 			values = new ArrayList<T>();
 			i=countFrom;
 			for (Ranker<T> r: arr)
 			{
 				values.add(r.value);
 				refs.put(r.value, i++);
 			}
 		}
 		
 		int id(T e)
 		{
 			assert(refs.containsKey(e));
 			return refs.get(e);
 		}
 		
 		public String toString()
 		{
 			return String.valueOf(refs);
 		}
 		
 		int size()
 		{
 			return countFrom + refs.size();
 		}
 	}
 	
 	/**
 	 * organize the pools so the most commonly referenced elements
 	 * have the lowest indexes.
 	 * 
 	 * TODO - remove dup namespaces from nssets & remove dup nssets
 	 * 
 	 * @author edwsmith
 	 */
 	class Abc
 	{
 		Pool<Integer> intPool = new Pool<Integer>(1);
 		Pool<Long> uintPool = new Pool<Long>(1);
 		Pool<Double> doublePool = new Pool<Double>(1);
 		Pool<String> stringPool = new Pool<String>(1);
 		Pool<Namespace> nsPool = new Pool<Namespace>(1);
 		Pool<Nsset> nssetPool = new Pool<Nsset>(1);
 		Pool<Name> namePool = new Pool<Name>(1);
 		Pool<Method> methodPool1 = new Pool<Method>(0);
 		Pool<Method> methodPool2 = new Pool<Method>(0);
 		Pool<Metadata> metaPool = new Pool<Metadata>(0);
 		int bodyCount;
 		boolean haveNatives;
 		
 		List<Type> scripts = new ArrayList<Type>();
 		List<Type> classes = new ArrayList<Type>();
 		
 		int typeRef(Typeref tref)
 		{
 			return typeRef(tref.t);
 		}
 		
 		int typeRef(Type t)
 		{
 			if (t == ANY)
 				return 0;
 			else if(t.emitAsAny()) {
 				verboseStatus("Emitting: " + t + " as any");
 				return 0;
 			} else
 				return namePool.id(t.name);
 		}
 		
 		Pool<Method> poolFor(Method m)
 		{
 			// put abc methods first, native methods second.
 			return m.isNative() ? methodPool2 : methodPool1;
 		}
 		
 		int methodId(Method m)
 		{
 			return poolFor(m).id(m);
 		}
 
 		void addScript(Type s)
 		{
 			// global object type
 			addMethod(s.init);
 			addTraits(s.defs);
 			scripts.add(s);
 		}
 		
 		void addClass(Type c)
 		{
 			// instance type
 			Type t = c.itype;
 			addName(t.name);
 			if (t.base != NULL)
 				addTypeRef(t.base);
 			if (t.hasProtectedNs())
 				addNamespace(t.protectedNs);
 			for (Type i: t.interfaces)
 				addTypeRef(i);
 			addMethod(t.init);
 			addTraits(t.defs);
 
 			// class type
 			addMethod(c.init);
 			addTraits(c.defs);
 			classes.add(c);
 		}
 		
 		int classId(Type c)
 		{
 			return classes.indexOf(c);
 		}
 		
 		int scriptId(Type c)
 		{
 			return scripts.indexOf(c);
 		}
 				
 		void addTraits(Symtab<Binding> defs)
 		{
 			for (Binding b: defs.values())
 			{
 				addName(b.name);
 				switch (b.kind())
 				{
 				case TRAIT_Class:
 					addClass(b.type.t);
 					break;
 				case TRAIT_Var:
 				case TRAIT_Const:
 					addTypeRef(b.type);
 					if (b.value != null)
 					{
 						addConst(b.value);
 					}
 					break;
 				case TRAIT_Method:
 				case TRAIT_Getter:
 				case TRAIT_Setter:
 					addMethod(b.method);
 					break;
 				}
 				
 				/*if (b.hasMetadata())
 					for (Metadata md : b.md)
 						addMetadata(md);*/
 			}
 		}
 		
 		void addMetadata(Metadata md)
 		{
 			if (metaPool.add(md) == 1)
 			{
 				stringPool.add(md.name);
 				for (Attr a: md.attrs)
 				{
 					stringPool.add(a.name);
 					stringPool.add(a.value);
 				}
 			}
 		}
 		
 		void addConst(Object value)
 		{
 			if (value instanceof Integer)
 				intPool.add(intValue(value));
 			else if (value instanceof Long)
 				uintPool.add(uintValue(value));
 			else if (value instanceof Double)
 				doublePool.add(doubleValue(value));
 			else if (value instanceof String)
 				stringPool.add((String)value);
 			else if (value instanceof Namespace)
 				addNamespace((Namespace) value);
 		}
 		
 		int constId(int kind, Object value)
 		{
 			switch (kind)
 			{
 			case CONSTANT_Integer:
 				return intPool.id(intValue(value));
 			case CONSTANT_UInteger:
 				return uintPool.id(uintValue(value));
 			case CONSTANT_Utf8:
 				return stringPool.id((String)value);
 			case CONSTANT_Double:
 				return doublePool.id(doubleValue(value));
 			case CONSTANT_Namespace:
 				return nsPool.id((Namespace)value);
 			}
 			return 0;
 		}
 		
 		int constKind(Object value)
 		{
 			if (value instanceof Integer)
 				return CONSTANT_Integer;
 			if (value instanceof Long)
 				return CONSTANT_UInteger;
 			if (value instanceof Double)
 				return CONSTANT_Double;
 			if (value instanceof String)
 				return CONSTANT_Utf8;
 			if (value instanceof Namespace)
 				return ((Namespace)value).kind;
 			if (value == TRUE)
 				return CONSTANT_True;
 			if (value == FALSE)
 				return CONSTANT_False;
 			if (value == UNDEFINED)
 				return CONSTANT_Void;
 			if (value == NULL)
 				return CONSTANT_Null;
 			return 0;
 		}
 		
 		void addNamespace(Namespace ns)
 		{
 			if (nsPool.add(ns) == 1)
 			{
 				if (!ns.isPrivateOrInternal())
 					stringPool.add(ns.uri);
 			}
 		}
 		
 		void addNsset(Nsset nsset)
 		{
 			if (nssetPool.add(nsset) == 1)
 				for (Namespace ns: nsset)
 					addNamespace(ns);
 		}
 		
 		void addName(Name n)
 		{
 			if (namePool.add(n) == 1)
 				switch (n.kind)
 				{
 				case CONSTANT_Multiname:
 				case CONSTANT_MultinameA:
 					addNsset(n.nsset);
 					stringPool.add(n.name);
 					break;
 				case CONSTANT_Qname:
 				case CONSTANT_QnameA:
 					addNamespace(n.nsset(0));
 					stringPool.add(n.name);
 					break;
 				case CONSTANT_RTQname:
 				case CONSTANT_RTQnameA:
 					stringPool.add(n.name);
 					break;
 				case CONSTANT_MultinameL:
 				case CONSTANT_MultinameLA:
 					addNsset(n.nsset);
 					break;
 				}
 		}
 		
 		void addTypeRef(Typeref tref)
 		{
 			addTypeRef(tref.t);
 		}
 		void addTypeRef(Type t)
 		{
 			if (t != ANY && !t.emitAsAny())
 				addName(t.name);
 		}
 		
 		void addMethod(Method m)
 		{
 			if (poolFor(m).add(m) > 1) 
 				return;
 			
 			if (m.entry != null)
 				bodyCount++;
 			
 			addTypeRef(m.returns.t);
 			for (int i=1, n=m.params.length; i < n; i++)
 				addTypeRef(m.params[i]);
 			
 			if (PRESERVE_METHOD_NAMES)	
 				addName(m.debugName); // debug name
 			
 			if (m.hasOptional())
 				for (Object v: m.values)
 					if (v != null)
 						addConst(v);
 			
 			if (!STRIP_DEBUG_INFO)
 			{
 				if (m.hasParamNames())
 					for (int i=1; i < m.paramNames.length; i++)
 						addName(m.paramNames[i]);
 			}
 			
 			for (Handler t: m.handlers)
 			{
 				addName(t.name);
 				addTypeRef(t.type);
 			}
 
 			// native and interface methods don't have a body
 			haveNatives |= m.isNative();
 			if (m.entry == null)
 				return;
 
 			for (Block b: dfs(m.entry.to))
 			{
 				for (Expr e: b)
 				{
 					switch (e.op)
 					{
 					case OP_debugfile:
 						if (!STRIP_DEBUG_INFO)
 							addConst(e.value);
 						break;
 					case OP_pushint:
 					case OP_pushuint:
 					case OP_pushstring:
 					case OP_pushnamespace:
 					case OP_pushdouble:
 					case OP_dxns:
 						addConst(e.value);
 						break;
 					case OP_istype:
 					case OP_astype:
 					case OP_findproperty:
 					case OP_findpropstrict:
 					case OP_callproperty:
 					case OP_callproplex:
 					case OP_callsuper:
 					case OP_callsupervoid:
 					case OP_constructprop:
 					case OP_getproperty:
 					case OP_setproperty:
 					case OP_deleteproperty:
 					case OP_getdescendants:
 					case OP_coerce:
 					case OP_getlex:
 					case OP_finddef:
 					case OP_callpropvoid:
 					case OP_initproperty:
 						addName(e.ref);
 						break;
 					case OP_newfunction:
 					case OP_callstatic:
 						addMethod(e.m);
 						break;
 					}
 				}
 			}
 			addTraits(m.activation.t.defs);
 		}
 		
 		void sort()
 		{
 			verboseStatus("NAMES RANK " + namePool.refs);
 			intPool.sort();
 			uintPool.sort();
 			doublePool.sort();
 			stringPool.sort();
 			nsPool.sort();
 			nssetPool.sort();
 			namePool.sort();
 			metaPool.sort();
 			methodPool1.sort();
 			methodPool2.countFrom = methodPool1.size();
 			methodPool2.sort();
 			verboseStatus("NAMES " + namePool.values);
 			
 			// topological sort of the classes, base classes come first
 			TreeSet<Type> cs = new TreeSet<Type>(new Comparator<Type>()
 			{
 				public int compare(Type a, Type b)
 				{
 					if (a == b) return 0;
 // no, this is subtly wrong: we might be comparing two classes that have no parent-child relationship
 // (eg SyntaxError and EvalError). this logic will push UP subclasses and push DOWN no-rel-classes and superclasses.
 // it's more reliable to push DOWN superclasses and UP subclasses and no-rel-classes. (we can't just return 0
 // for no-rel because TreeSet will assume they are identical and eliminate one...)
 //					else if (istype(a.itype, b.itype)) return 1;
 //					else return -1;
 					else if (istype(b.itype, a.itype)) return -1;
 					else return 1;
 				}
 			});
 			cs.addAll(classes);
 			classes.clear();
 			classes.addAll(cs);
 		}
 	}
 	
 	int argc(Expr e)
 	{
 		switch (e.op)
 		{
 		case OP_callproperty:
 		case OP_callproplex:
 		case OP_callpropvoid:
 		case OP_callsuper:
 		case OP_callsupervoid:
 		case OP_constructprop:
 			return e.args.length - refArgc[e.ref.kind] - 1;
 		case OP_applytype:
 		case OP_callstatic:
 		case OP_callmethod:
 		case OP_constructsuper:
 		case OP_construct:
 			return e.args.length-1;
 		case OP_call:
 			return e.args.length-2;
 		case OP_newarray:
 			return e.args.length;
 		case OP_newobject:
 			assert(e.args.length % 2 == 0);
 			return e.args.length/2;
 		default:
 			assert(false);
 		}
 		return 0;
 	}
 	
 	static class IndentingPrintWriter extends PrintWriter
 	{
 		int indent;
 		IndentingPrintWriter(Writer w)
 		{
 			super(w);
 		}
 		public void println()
 		{
 			super.println();
 			for (int i=0; i < indent; i++)
 				print("    ");
 		}
 	}
 	
 	byte[] emit(InputAbc a, String filename, List<Integer> initScripts, boolean no_c_gen) throws IOException
 	{
 		// schedule everything that is reachable.  this will assign new id's to
 		// stuff, then we can write it all out in the right order.
 		Abc abc = new Abc();
 		for (Type s: a.scripts)
 			abc.addScript(s);
 		
 		abc.sort();
 		
 		String scriptname = filename.substring(0,filename.lastIndexOf('.'));
 		byte[] data = emitAbc(abc);
 		OutputStream out = new FileOutputStream(scriptname+".abc2");
 		try
 		{
 			out.write(data);
 		}
 		finally
 		{
 			out.close();
 		}
 		
 		if (abc.haveNatives && !no_c_gen)
 		{
 			PrintWriter out_h = new PrintWriter(new FileWriter(scriptname+".h2"));
 			IndentingPrintWriter out_c = new IndentingPrintWriter(new FileWriter(scriptname+".cpp2"));
 			try
 			{
 				emitSource(abc, scriptname, data, initScripts, out_h, out_c);
 			}
 			finally
 			{
 				out_c.close();
 				out_h.close();
 			}
 		}
 		return data;
 	}
 	
 	void emitSource(Abc abc, String name, byte[] data, List<Integer> initScripts,
 			PrintWriter out_h, IndentingPrintWriter out_c)
 	{
 		// header file - definitions & native method decls
 		out_h.println("/* machine generated file -- do not edit */");
  		out_h.println("namespace avmplus {");
 
  		out_h.println("AVMPLUS_NATIVEMAP_DECLARE("+name+", "+ abc.methodPool1.size()+")");
 
 		out_c.println("/* machine generated file -- do not edit */");
  		out_c.println("namespace avmplus {");
 		
 		// output vm creation init method
  		out_h.println("extern AvmInstance "+name+"_init(GC* gc, const AvmConfiguration& c, const uint8_t* abc_data=NULL, size_t length=0);");
 		
 		// data buf decl only visible to init method
 		out_c.println("extern const uint8_t "+name+"_abc_data["+data.length+"];");
  		out_c.println("AvmInstance "+name+"_init(GC* gc, const AvmConfiguration& c, const uint8_t* abc_data/*=NULL*/, size_t length/*=0*/)");
  		out_c.printf("{\n\tif (abc_data==NULL) { abc_data = %s_abc_data; length = %s; }\n", name, data.length);
  		out_c.printf("\tAvmInstance _vm = AvmInit(gc, c, abc_data, length, %s_natives, %s_offset);\n", name, name);
 		
 		for(int i: initScripts) {
  			out_c.printf("\tAvmInitScript(_vm, %d);\n", i);
 		}
 		
 		out_c.printf("\treturn _vm;\n}\n");
 		
 		StringWriter b = new StringWriter();
 		PrintWriter out_t = new PrintWriter(b);
 		Map<Integer,String> impls = new TreeMap<Integer,String>();
 
 		for (Type s: abc.scripts)
 			emitSourceTraits("", abc, s, out_h, impls, out_t, out_c);
 		
 		out_c.print("AVMPLUS_NATIVEMAP_BEGIN("+name+")");
 		out_c.indent++;
 		out_c.println();
 		for (int id: impls.keySet())
 		{
 			String s = impls.get(id);
 			if (s.charAt(0) == 'f')
 				out_c.println("AVMPLUS_NATIVEMAP_FORTHMETHOD("+s.substring(1)+")");
 			else
 				out_c.println("AVMPLUS_NATIVEMAP_CMETHOD("+s.substring(1)+")");
 		}
 		out_c.indent--;
 		out_c.println("AVMPLUS_NATIVEMAP_END()");
 		out_c.println();
 		
 		// put thunks in cpp file
 		out_t.flush();
 		out_c.println(b);
 		out_c.println();
 		
 		// cpp file - abc data, thunks
 		out_c.print("const uint8_t "+name+"_abc_data["+data.length+"] = {");
 		out_c.indent++;
 		out_c.println();
 		for (int i=0, n=data.length; i < n; i++)
 		{
 			int x = data[i] & 255;
 			if (x < 10) out_c.print("  ");
 			else if (x < 100) out_c.print(' ');
 			out_c.print(x);
 			if (i+1 < n) out_c.print(", ");
 			if (i%16 == 15) out_c.println();
 		}
 		out_c.indent--;
 		out_c.println("};");
 		out_c.println("} /* namespace avmplus */");
 
 		out_h.println("} /* namespace avmplus */");
 	}
 
 	void emitSourceTraits(String prefix, Abc abc, Type s, 
 			PrintWriter out_h, Map<Integer,String> impls, PrintWriter out_t, PrintWriter out_c)
 	{
 		out_h.println();
 
 		assert(!s.init.isNative());
 		for (Binding b: s.defs.values())
 		{
 			Namespace ns = b.name.nsset(0);
 			String id = prefix + propLabel(b, ns);
 			boolean isNative = false;
 			String ctype = null;
 			if (b.md.length > 0)
 				for (Metadata md : b.md)
 					if (md.name.equals("native"))
 					{
 						isNative = true;
 						for (Attr a: md.attrs)
 							if (a.name.equals("type"))
 								ctype = a.value;
 					}
 
 			if (b.method != null) {
 				if(b.method.isNative())
 					emitSourceMethod(prefix, abc, b, ns, out_h, impls, out_t, s.obscure_natives);
 				else if(isNative && !s.obscure_natives) {
 					int scriptId = abc.scriptId(s);
 					if(scriptId == -1)
 						throw new RuntimeException("Only scripts can have native callins");
 					emitCallinMethod(b, ns, abc.scriptId(s), out_h, out_c);
 				}
 			} else if (isClass(b))
 				emitSourceClass(abc, out_h, out_c, impls, out_t, b, ns, s.obscure_natives);
 			else if (isSlot(b) && isNative)
 				emitSourceSlot(prefix, abc, b, ns, id, ctype, out_h, out_t, s.obscure_natives);
 		}
 	}
 	
 	void emitSourceSlot(String prefix, Abc abc, Binding b, Namespace ns, String id, String ctype, PrintWriter out_h, PrintWriter out_t, boolean obscure_natives)
 	{
 		if (obscure_natives)
 			return;
 			
 		if (isAtom(b.type.t))
 		{
 			if (ctype != null)
 			{
 				// native GCObject's must be stored as atoms, field type must be *.
 				if (b.type.t != ANY || !ns.isPrivateOrInternal())
 					throw new RuntimeException("native field "+id+" must be private or internal and type *");
 				out_h.println("AVMPLUS_NATIVE_SLOT_DECL_GC("+ctype+","+b.offset+","+id+")");
 			}
 			else
 			{
 				out_h.println("AVMPLUS_NATIVE_SLOT_DECL_ATOM("+b.offset+","+id+")");
 			}
 		}
 		else if (b.type.t.emitAsAny())
 		{
 			ctype = ctype(b.type);
 			out_h.println("AVMPLUS_NATIVE_SLOT_DECL_GC("+ctype+","+b.offset+","+id+")");
 		}
 		else if (b.type.t.numeric)
 		{
 			ctype = ctype(b.type);
 			out_h.println("AVMPLUS_NATIVE_SLOT_DECL_PRIM("+ctype+","+b.offset+","+id+")");
 		}
 		else
 		{
 			ctype = ctype(b.type);
 			out_h.println("AVMPLUS_NATIVE_SLOT_DECL_RC("+ctype+","+b.offset+","+id+")");
 		}
 	}
 	
 	void emitSourceClass(Abc abc, PrintWriter out_h, PrintWriter out_c, Map<Integer,String>impls, PrintWriter out_t,
 			Binding b, Namespace ns, boolean obscure_natives)
 	{
 		String label = (ns.isPublic()||ns.isInternal()) ? b.name.name : 
 			ns.isProtected() ? "protected_"+b.name.name :
 			namespaceNames.containsKey(ns) ? namespaceNames.get(ns)+"_"+b.name.name : 
 			(ns.uri.replace(' ', '_').replace('.','_').replace('$','_')+'_'+b.name.name);
 
 		Type c = b.type.t;
 
 		if (!obscure_natives)
 		{
 			out_h.println("const int abcclass_"+ label + " = " + abc.classId(c) + ";");
 		}
 
 		emitSourceTraits(label+"_", abc, c, out_h, impls, out_t, out_c);
 		emitSourceTraits(label+"_", abc, c.itype, out_h, impls, out_t, out_c);
 	}
 	
 	String ctype(Typeref tref)
 	{
 		Type t = tref.t;
 		if (t == VOID) return "void";
 		if (isAtom(t)) return "AvmBox";
 		if (t == INT) return "int32_t";
 		if (t == BOOLEAN) return "bool";	// no, this would be bad.
 		if (t == UINT) return "uint32_t";
 		if (t == STRING) return "AvmString";
 		if (t == NAMESPACE) return "AvmNamespace";
 		if (t == NUMBER) return "double";
 		if (t.base == null) return tref.nonnull().toString() + "*";
 		else return "AvmObject /*"+tref.toString()+"*/";
 	}
 
 	void emitSourceMethod(String prefix, Abc abc, 
 			Binding b, Namespace ns, PrintWriter out_h,
 			Map<Integer,String> impls, PrintWriter out_t, boolean obscure_natives)
 	{
 		Method m = b.method;
 
 		String impl = prefix + propLabel(b, ns);
 		if (isGetter(b))
 			impl += "_get";
 		else if (isSetter(b))
 			impl += "_set";
 		
 		String forthword = null;
 		if (b.md.length > 0)
 		{
 			for (Metadata md : b.md)
 			{
 				if (md.name.equals("forth"))
 				{
 					for (Attr a: md.attrs)
 					{
 						if (a.name.equals("word"))
 						{
 							forthword = a.value;
 							break;
 						}
 					}
 					if (forthword == null)
 						throw new RuntimeException("the forth metadata must specify the word attribute");
 					break;
 				}
 			}
 		}
 
 		if (forthword != null)
 		{
 			// note that forth methods are allowed to have optional parameters, but
 			// the default values will be ignored! it is up to the forth implementation
 			// to check and fill them in properly (that is, the optional args are allowed
 			// only to satisfy signature requirements).
 			
 			out_h.printf("AVMPLUS_FORTH_METHOD_DECL(%s, %s)\n", forthword, impl);
 
 			impls.put(abc.methodId(m), "f" + forthword);
 		}
 		else
 		{
 			if (m.hasOptional())
 			{
 				throw new RuntimeException("native methods may not have optional parameters: "+impl);
 			}
 
 			if (m.needsRest())
 			{
 				throw new RuntimeException("native methods may not have rest args: "+impl);
 			}
 
 			// create a C++ declaration for the native thunk.
 			createThunkArgs(out_h, impl, m, obscure_natives);
 			if(m.returns.t != VOID && m.returns.t.base == null && !isAtom(m.returns.t))
 				out_h.printf("AVMPLUS_NATIVE_METHOD_DECL_GCOBJ(%s, %s)\n", ctype(m.returns), impl);
 			else 
 				out_h.printf("AVMPLUS_NATIVE_METHOD_DECL(%s, %s)\n", ctype(m.returns), impl);
 
 			impls.put(abc.methodId(m), "n" + impl);
 		}
 
 	}
 
 	String propLabel(Binding b, Namespace ns)
 	{
 		return (ns.isPublic()||ns.isInternal()) ? b.name.name : 
 			ns.isPrivate() ? "private_"+b.name.name : 
 			ns.isProtected() ? "protected_"+b.name.name :
 				namespaceNames.get(ns)+"_"+b.name.name;
 	}
 
 	/**
 	 * create C++ code for a struct for the argtypes for this method
 	 * 
 	 * @param out_t
 	 * @param id
 	 * @param m
 	 */
 	void createThunkArgs(PrintWriter out_h, String id, Method m, boolean obscure_natives)
 	{
 
 		if (!m.hasParamNames() && m.params.length > 1)
 		{
 			throw new RuntimeException("native method " + id + " must be generated with debug info (have no fear, it will be stripped)");
 		}
 
 		out_h.println();
 		if (obscure_natives)
 		{
 			out_h.println("struct " + id + "_args;");
 			return;
 		}
 		
 		out_h.println("struct " + id + "_args");
 		out_h.println("{");
 
 		// args
 		int i=0;
 		for (int n=m.params.length; i < n; i++)
 		{
 			String argname = (i == 0) ? 
 								((m.params[i].toString().indexOf("$") >= 0) ? "classself" : "self") : 
 								m.paramNames[i].name;
 			if (m.params[i].t==NUMBER)
 			{
 				out_h.printf("    public: double %s;\n", argname);
 			}
 			else if (m.params[i].t==BOOLEAN)
 			{
 				out_h.printf("    public: int32_t %s_b; private: int32_t %s_pad; public: inline bool %s() const { return %s_b != 0; }\n", argname, argname, argname, argname);
 			}
 			else if (m.params[i].t==OBJECT || m.params[i].t==ANY)
 			{
 				out_h.printf("    public: AvmBoxArg %s;\n", argname);
 			}
 			else
 			{
 				out_h.printf("    public: %s %s; private: int32_t %s_pad; \n", ctype(m.params[i]), argname, argname);
 			}
 		}
 
 		out_h.printf("    public: AvmStatusOut status_out;\n");
 
 		out_h.println("};");
 	}
 
 	void emitCallinMethod(Binding b, Namespace ns, int class_id, 
 			PrintWriter out_h, PrintWriter out_c)
 	{
 		Method m = b.method;
 		String impl = ns.uri.replace('.', '_') + "_" + b.name.name;
 		writeCallin(m, class_id, impl, out_h, out_c);
 	}	
 
 	String boxSetter(Type t)
 	{
 		if(t == INT)
 			return "Int";
 		else if(t == UINT)
 			return "Uint";
 		else if(t == BOOLEAN)
 			return "Bool";
 		else if(t == STRING)
 			return "String";
 		else if(t == NAMESPACE)
 			return "Namespace";
 		else if(t == NUMBER)
 			return "Double";		
 		else if(t.base == null) // same test use to emit ANY for native slots, valid?
 			return "GCObject";
 		return "Object";
 	}
 	
 	void writeCallin(Method m, int script_id, String impl, PrintWriter out_h, PrintWriter out_c)
 	{
 		StringWriter bodySW = new StringWriter();
 		StringWriter declSW = new StringWriter();
 		PrintWriter decl = new PrintWriter(declSW);
 		PrintWriter body = new PrintWriter(bodySW);
 		
 		decl.printf("%s %s(AvmInstance vm", ctype(m.returns), impl);
 		body.print("\n{\n");
 		if(m.params.length > 1)
 			body.printf("\tAvmBox _args[%d];\n", m.params.length - 1);
 		// args
 		int i=1;
 		for (int n=m.params.length; i < n; i++)
 		{
 			Typeref t = m.params[i];
 			decl.printf(", %s %s", ctype(t), m.paramNames[i]);
 			body.printf("\t_args[%d] = AvmBox%s(%s);\n", i-1, boxSetter(t.t), m.paramNames[i]);
 		}
 		decl.print(")"); 
  		body.print("\t");
 		if(m.returns.t != VOID)
 			body.print("const AvmBox _returnBox = ");
 		
 		body.printf("\tAvmInvokeCallin(vm, %d, %d, %d, %s);\n", 
 				script_id, m.emit_id, m.params.length - 1, m.params.length > 1 ? "(AvmBox*)&_args" : "NULL");
 		
 		if(m.returns.t != VOID)			
 			body.printf("\treturn AvmUnBox%s(_returnBox);\n", boxSetter(m.returns.t));
 		else 
 			body.print(";\n");
 		body.print("}\n");
 		
 		out_h.printf("%s;\n", declSW.getBuffer());
 		out_c.print(declSW.getBuffer());
 		out_c.print(bodySW.getBuffer());
 	}
 	
 	byte[] emitAbc(Abc abc) throws IOException
 	{
 		AbcWriter w = new AbcWriter();
 		w.writeU16(16);
 		w.writeU16(46);
 
 		int pos = w.size();
 		w.writeU30(abc.intPool.size());
 		for (int x: abc.intPool.values)
 			w.writeU30(x);
 		
 		verboseStatus("ints count "+abc.intPool.size()+ " size " + (w.size()-pos));
 		pos = w.size();
 		
 		w.writeU30(abc.uintPool.size());
 		for (long x: abc.uintPool.values)
 			w.writeU30((int)x);
 
 		verboseStatus("uints count "+abc.uintPool.size()+ " size " + (w.size()-pos));
 		pos = w.size();
 		
 		verboseStatus("doubles "+abc.doublePool.size());
 		w.writeU30(abc.doublePool.size());
 		for (double x: abc.doublePool.values)
 			w.write64(Double.doubleToLongBits(x));
 		
 		verboseStatus("double count "+abc.doublePool.size()+ " size " + (w.size()-pos));
 		pos = w.size();
 
 		w.writeU30(abc.stringPool.size());
 		for (String s: abc.stringPool.values)
 		{
 			w.writeU30(s.length());
 			w.write(s.getBytes("UTF-8"));
 		}
 		verboseStatus("strings count "+abc.stringPool.size()+ " size " + (w.size()-pos));
 		pos = w.size();
 		
 		w.writeU30(abc.nsPool.size());
 		for (Namespace ns: abc.nsPool.values)
 			emitNamespace(abc, w, ns);
 		verboseStatus("ns count "+abc.nsPool.size()+ " size " + (w.size()-pos));
 		pos = w.size();
 		
 		w.writeU30(abc.nssetPool.size());
 		for (Nsset nsset: abc.nssetPool.values)
 		{
 			w.writeU30(nsset.length);
 			for (Namespace ns: nsset)
 				w.writeU30(abc.nsPool.id(ns));
 		}
 		verboseStatus("nsset count "+abc.nssetPool.size()+ " size " + (w.size()-pos));
 		pos = w.size();
 		
 		w.writeU30(abc.namePool.size());
 		for (Name n: abc.namePool.values)
 		{
 			w.write(n.kind);
 			switch (n.kind)
 			{
 			case CONSTANT_TypeName:
 				throw new RuntimeException("TODO CONSTANT_TypeName is not supported on output yet, only input");
 			case CONSTANT_Qname:
 			case CONSTANT_QnameA:
 				w.writeU30(abc.nsPool.id(n.nsset(0)));
 				w.writeU30(abc.stringPool.id(n.name));
 				break;
 			case CONSTANT_Multiname:
 			case CONSTANT_MultinameA:
 				w.writeU30(abc.stringPool.id(n.name));
 				w.writeU30(abc.nssetPool.id(n.nsset));
 				break;
 			case CONSTANT_RTQname:
 			case CONSTANT_RTQnameA:
 				w.writeU30(abc.stringPool.id(n.name));
 				break;
 			case CONSTANT_MultinameL:
 			case CONSTANT_MultinameLA:
 				w.writeU30(abc.nssetPool.id(n.nsset));
 				break;
 			case CONSTANT_RTQnameL:
 			case CONSTANT_RTQnameLA:
 				break;
 			default:
 				assert (false);
 			}
 		}
 		verboseStatus("name count "+abc.namePool.size()+ " size " + (w.size()-pos));
 		pos = w.size();
 			
 		w.writeU30(abc.methodPool2.size());
 		int method_id=0;
 		for (Method m: abc.methodPool1.values)
 			emitMethod(abc, w, method_id++, m);
 		for (Method m: abc.methodPool2.values)
 			emitMethod(abc, w, method_id++, m);
 		
 		w.writeU30(abc.metaPool.size());
 		for (Metadata md: abc.metaPool.values)
 		{
 			w.writeU30(abc.stringPool.id(md.name));
 			w.writeU30(md.attrs.length);
 			for (Attr a: md.attrs)
 				w.writeU30(abc.stringPool.id(a.name));
 			for (Attr a: md.attrs)
 				w.writeU30(abc.stringPool.id(a.value));
 		}
 		
 		w.writeU30(abc.classes.size());
 		for (Type c: abc.classes)
 		{
 			Type t = c.itype;
 			w.writeU30(abc.namePool.id(t.name));
 			w.writeU30(abc.typeRef(t.base));
 			w.write(t.flags);
 			if (t.hasProtectedNs())
 				w.writeU30(abc.nsPool.id(t.protectedNs));
 			w.writeU30(t.interfaces.length);
 			for (Type i: t.interfaces)
 				w.writeU30(abc.typeRef(i));
 			w.writeU30(abc.methodId(t.init));
 			emitTraits(w, abc, t);
 		}
 		
 		for (Type c: abc.classes)
 		{
 			w.writeU30(abc.methodId(c.init));
 			emitTraits(w, abc, c);
 		}
 		
 		w.writeU30(abc.scripts.size());
 		for (Type s: abc.scripts)
 		{
 			w.writeU30(abc.methodId(s.init));
 			emitTraits(w, abc, s);
 		}
 		
 		w.writeU30(abc.bodyCount);
 		emitBodies(abc, w, abc.methodPool1);
 		emitBodies(abc, w, abc.methodPool2);
 	
 		return w.toByteArray();
 	}
 
 	void emitNamespace(Abc emitNamespace, AbcWriter w, Namespace ns)
 	{
 		if (ns.isPrivateOrInternal())
 		{
 			// internal -> private == sealed packages!
 			w.write(CONSTANT_PrivateNamespace);
 			w.writeU30(0);
 		}
 		else
 		{
 			w.write(ns.kind);
 			w.writeU30(emitNamespace.stringPool.id(ns.uri));
 		}
 	}
 
 	void emitBodies(Abc abc, AbcWriter w, Pool<Method> pool) throws IOException
 	{
 		for (Method m: pool.values)
 		{
 			if (m.entry == null)
 				continue;
 			w.writeU30(m.emit_id);
 			w.writeU30(m.max_stack);
 			w.writeU30(m.local_count);
 			if (m.cx != null && m.cx.scopes != null)
 			{
 				w.writeU30(m.cx.scopes.length); // init_scope_depth
 				w.writeU30(m.cx.scopes.length+m.max_scope); // max_scope_depth
 			}
 			else
 			{
 				w.writeU30(0); // init_scope_depth
 				w.writeU30(m.max_scope); // max_scope_depth
 			}
 			
 			emitCode(w, abc, m);
 			
 			emitTraits(w, abc, m.activation.t);
 		}
 	}
 
 	void emitMethod(Abc abc, AbcWriter w, int method_id, Method m)
 	{
 		m.emit_id = method_id;
 		verboseStatus("METHOD " + method_id + " was " + m.id);
 		w.writeU30(m.params.length-1);
 		w.writeU30(abc.typeRef(m.returns));
 		for (int i=1, n=m.params.length; i < n; i++)
 			w.writeU30(abc.typeRef(m.params[i]));
 		if (PRESERVE_METHOD_NAMES)
 			w.writeU30(abc.namePool.id(m.debugName));
 		else
 			w.writeU30(0);
 		
 		int flags = m.flags;
 		if (STRIP_DEBUG_INFO)
 			flags &= ~METHOD_HasParamNames;
 		w.write(flags);
 		if (m.hasOptional())
 		{
 			int optional_count = 0;
 			for (Object v : m.values) 
 				if (v != null)
 					optional_count++;
 			assert(optional_count > 0);
 			w.writeU30(optional_count);
 			for (int j = m.params.length - optional_count; j < m.params.length; j++)
 			{
 				int kind = abc.constKind(m.values[j]);
 				w.writeU30(abc.constId(kind, m.values[j])); // index
 				w.write(kind); // kind
 			}
 		}
 		if ((flags & METHOD_HasParamNames) != 0)
 		{
 			for (int i=1; i < m.paramNames.length; i++)
 				w.writeU30(abc.stringPool.id(m.paramNames[i].name));
 		}
 	}
 	
 	int intValue(Object o)
 	{
 		return ((Number)o).intValue();
 	}
 	
 	long uintValue(Object o)
 	{
 		return ((Number)o).longValue() & 0xFFFFFFFFL;
 	}
 	
 	double doubleValue(Object o)
 	{
 		return o instanceof Number ? ((Number)o).doubleValue() : Double.NaN;
 	}
 	
 	boolean booleanValue(Object o)
 	{
 		if (o instanceof Boolean)
 			return o == TRUE;
 		if (o instanceof String || o instanceof Namespace)
 			return true;
 		if (o == NULL || o == UNDEFINED)
 			return false;
 		return doubleValue(o) != 0;
 	}
 	
 	String stringValue(Object v0)
 	{
 		// TODO ES3 compatible double format
 		return String.valueOf(v0);
 	}
 
 	void emitBlock(AbcWriter out, Block b, Abc abc)
 	{
 		// emit all but the last instruction of each block.
 		for (Expr e: b)
 		{
 			if (e.succ != null)
 				break;
 			out.write(e.op);
 			switch (e.op)
 			{
 			case OP_hasnext2:
 				out.writeU30(e.imm[0]);
 				out.writeU30(e.imm[1]);
 				break;
 			case OP_findproperty:
 			case OP_findpropstrict:
 			case OP_getlex:
 			case OP_getsuper: case OP_setsuper:
 			case OP_getproperty: case OP_setproperty:
 			case OP_deleteproperty: case OP_getdescendants:
 			case OP_initproperty:
 			case OP_istype:
 			case OP_coerce:
 			case OP_astype:
 			case OP_finddef:
 				out.writeU30(abc.namePool.id(e.ref));
 				break;
 			case OP_callproperty:
 			case OP_callproplex:
 			case OP_callpropvoid:
 			case OP_callsuper:
 			case OP_callsupervoid:
 			case OP_constructprop:
 				out.writeU30(abc.namePool.id(e.ref));
 				out.writeU30(argc(e));
 				break;
 			case OP_constructsuper:
 			case OP_call:
 			case OP_construct:
 			case OP_newarray:
 			case OP_newobject:
 				out.writeU30(argc(e));
 				break;
 			case OP_getlocal:
 			case OP_setlocal:
 				if (e.imm[0] < 4)
 				{
 					out.rewind(1);
 					out.write((e.op == OP_getlocal ? OP_getlocal0 : OP_setlocal0)+e.imm[0]);
 					break;
 				}
 				// else fall through
 			case OP_getslot:
 			case OP_setslot:
 			case OP_kill:
 			case OP_inclocal:
 			case OP_declocal:
 			case OP_inclocal_i:
 			case OP_declocal_i:
 			case OP_newcatch:
 			//case OP_getglobalslot:
 			//case OP_setglobalslot:
 				out.writeU30(e.imm[0]);
 				break;
 			case OP_newclass:
 				out.writeU30(abc.classId(e.c));
 				break;
 			case OP_newfunction:
 				out.writeU30(abc.methodId(e.m));
 				break;
 			case OP_applytype:
 				out.writeU30(argc(e));
 				break;
 			case OP_callstatic:
 			//case OP_callmethod:
 				out.writeU30(abc.methodId(e.m));
 				out.writeU30(argc(e));
 				break;
 			case OP_pushshort:
 				out.writeU30(intValue(e.value));
 				break;
 			case OP_pushbyte:
 				out.write(intValue(e.value));
 				break;
 			case OP_getscopeobject:
 				out.write(e.imm[0]);
 				break;
 			case OP_pushstring:
 			case OP_dxns:
 				out.writeU30(abc.stringPool.id((String)e.value));
 				break;
 			case OP_debugfile:
 				if (STRIP_DEBUG_INFO)
 					throw new RuntimeException("impossible");
 				out.writeU30(abc.stringPool.id((String)e.value));
 				break;
 			case OP_pushnamespace:
 				out.writeU30(abc.nsPool.id((Namespace)e.value));
 				break;
 			case OP_pushint:
 				out.writeU30(abc.intPool.id(intValue(e.value)));
 				break;
 			case OP_pushuint:
 				out.writeU30(abc.uintPool.id(uintValue(e.value)));
 				break;
 			case OP_pushdouble:
 				out.writeU30(abc.doublePool.id(doubleValue(e.value)));
 				break;
 			case OP_debugline:
 			case OP_bkptline:
 				if (STRIP_DEBUG_INFO)
 					throw new RuntimeException("impossible");
 				out.writeU30(e.imm[0]);
 				break;
 			case OP_debug:
 				if (STRIP_DEBUG_INFO)
 					throw new RuntimeException("impossible");
 				out.write(e.imm[0]);
 				out.writeU30(e.imm[1]);
 				out.write(e.imm[2]);
 				out.writeU30(e.imm[3]);
 				break;
 			}
 		}
 	}
 	
 	void emitCode(AbcWriter out, Abc abc, Method m) throws IOException
 	{
 		Map<Block,Integer> padding = new HashMap<Block,Integer>();
 		Deque<Block> code = schedule(m.entry.to);
 		Map<Block,AbcWriter> writers = new HashMap<Block,AbcWriter>();
 		
 		// find the back edges to see where we need labels
 		BitSet labels = new BitSet();
 		
 		for (Block b: code)
 			for (Edge e: b.succ())
 				if ( e.isBackedge() )
 					labels.set(e.to.id);
 
 		// emit the code and leave room for the branch offsets,
 		// and compute the final position of each block.
 		Map<Block,Integer> pos = new HashMap<Block,Integer>();
 		Map<Block,Integer> blockends = new HashMap<Block,Integer>();
 		int code_len = 0;
 		Deque<Block> work = new ArrayDeque<Block>(code);
 		while (!work.isEmpty())
 		{
 			Block b = work.removeFirst();
 			pos.put(b,code_len);
 			AbcWriter w = new AbcWriter();
 			writers.put(b, w);
 			if (labels.get(b.id))
 				w.write(OP_label);
 			emitBlock(w, b, abc);
 			
 			code_len += w.size();
 			Expr last = b.last();
 			if (last.succ.length == 0)
 			{
 				 // returnvoid, returnvalue, or throw
 				w.write(last.op);
 				code_len++;
 			}
 			else if (isJump(last))
 			{
 				if (work.isEmpty() || last.succ[0].to != work.peekFirst())
 				{
 					// target is not fall through block so we'll emit a jump.
 					code_len += 4;
 					padding.put(b, 4);
 				}
 			}
 			else if (isBranch(last))
 			{
 				if (work.isEmpty() || last.succ[0].to != work.peekFirst())
 				{
 					code_len += 8;
 					padding.put(b, 8);
 				}
 				else
 				{
 					code_len += 4;
 					padding.put(b, 4);
 				}
 			}
 			else
 			{
 				assert(last.op == OP_lookupswitch);
 				assert(false);//TODO
 			}
 			blockends.put(b,code_len);
 		}
 
 		out.writeU30(code_len);
 		int code_start = out.size();
 		for (Block b: code)
 		{
 			writers.get(b).writeTo(out);
 			if (padding.containsKey(b))
 			{
 				Expr last = b.last();
 				if (isBranch(last))
 				{
 					emitBranch(out, last.op, last.succ[1].to, code_start, pos);
 					padding.put(b, padding.get(b)-4);
 				}
 				if (padding.get(b) == 4)
 				{
 					emitBranch(out, OP_jump, last.succ[0].to, code_start, pos);
 				}
 			}
 		}
 
 		out.writeU30(m.handlers.length);
 		for (Handler h: m.handlers)
 		{
 			int from = code_len;
 			int to = 0;
 			for (Block b: code)
 			{
 				for (Edge x: b.xsucc)
 				{
 					if (x.to == h.entry)
 					{
 						if (pos.get(b) < from)
 							from = pos.get(b);
 						if (blockends.get(b) > to)
 							to = blockends.get(b);
 					}
 				}
 			}
 			out.writeU30(from);
 			out.writeU30(to);
 			int off = pos.get(h.entry);
 			verboseStatus("handler "+h.entry+ " ["+from+","+to+")->"+off);
 			out.writeU30(off);
 			out.writeU30(abc.typeRef(h.type));
 			out.writeU30(abc.namePool.id(h.name));
 		}
 	}
 	
 	void emitBranch(AbcWriter out, int op, Block target, int code_start, Map<Block,Integer>pos)
 	{
 		out.write(op);
 		int to = code_start + pos.get(target);
 		int from = out.size()+3;
 		out.writeS24(to-from);
 	}
 	
 	boolean defaultValueChanged(Binding b)
 	{
 		return b.type.t.defaultValue != null && !b.type.t.defaultValue.equals(b.value);
 	}
 	
 	void emitTraits(AbcWriter out, Abc abc, Type t)
 	{
 		Symtab<Binding> defs = t.defs;
 		out.writeU30(defs.size());
 		for (Binding b: defs.values())
 		{
 			out.writeU30(abc.namePool.id(b.name));
 			out.write(b.flags_kind & ~(TRAIT_FLAG_metadata<<4));
 			switch (b.kind())
 			{
 			case TRAIT_Var:
 			case TRAIT_Const:
 				out.writeU30(b.slot); // abc format slot_id's count from 1
 				out.writeU30(abc.typeRef(b.type));
 				if (!defaultValueChanged(b))
 				{
 					// vm fills in default value based on type
 					out.writeU30(0);
 				}
 				else
 				{
 					// non-default default value
 					int kind = abc.constKind(b.value);
 					int id = abc.constId(kind, b.value);
 					out.writeU30(id);
 					if (id != 0)
 						out.write(kind);
 				}
 				break;
 			case TRAIT_Class:
 				out.writeU30(b.slot);
 				out.writeU30(abc.classId(b.type.t));
 				break;
 			case TRAIT_Method:
 			case TRAIT_Getter:
 			case TRAIT_Setter:
 				out.writeU30(b.slot);
 				out.writeU30(abc.methodId(b.method));
 				break;
 			default:
 				assert(false);
 			}
 		}
 	}
 
 	void optimize(Method m)
 	{
 		verboseStatus("OPTIMIZE "+m.id + " "+ m.name);
 
 		if(m.entry == null)
 			return;
 		
 		verboseStatus("BEFORE OPT");		
 		verboseStatus("BEFORE OPT");
 		print(dfs(m.entry.to));
 		
 		if (OUTPUT_DOT)
 			dot("-before",m);
 		
 		sccp(m);
 		dvn(m);
 		if (cfgopt(m))
 		{
 			verboseStatus("AFTER CFGOPT");
 			print(dfs(m.entry.to));
 			sccp(m);
 			dvn(m);
 		}
 	
 		// find operations to fold together.
 		fold(m);
 		
 		verboseStatus("AFTER FOLD");
 		print(dfs(m.entry.to));
 
 		if (OUTPUT_DOT)
 			dot("-after",m);
 		
 		insert_casts(m);
 		remove_phi(m);
 		printabc(schedule(m.entry.to));
 		verboseStatus("");
 	}
 	
 	public static byte[] optimize( byte[] raw_abc, java.util.Vector<String> imports )
 	throws java.io.IOException
 	{
 		GlobalOptimizer go = new GlobalOptimizer();
 	
 		//  TODO: ASC and GO data structures need to interoperate.
 		//  For now, just redo the imports.
 		for ( String import_name: imports )
 		{
 			go.new InputAbc().readAbc(load(import_name));
 		}
 	
 		InputAbc input_abc = go.new InputAbc();
 		input_abc.readAbc(raw_abc);
 	
 		go.optimize(input_abc);
 	
 		Abc abc = go.new Abc();
 	
 		for (Type s: input_abc.scripts)
 		{
 			abc.addScript(s);
 		}
 		
 		abc.sort();
 		
 		return go.emitAbc(abc);
 	}
 	
 	void fold(Method m)
 	{
 		Deque<Block> code = dfs(m.entry.to);
 		EdgeMap<Expr> uses = findUses(code);
 		for (Block b: code)
 		{
 			for (Expr e: b)
 			{
 				switch (e.op)
 				{
 				case OP_iftrue:
 				case OP_iffalse:
 				{
 					Expr a0 = e.args[0];
 					if (containsOnly(uses.get(a0),e))
 					{
 						if (false)
 							;
 						else if (a0.op == OP_not)
 							subsume_arg(e, (e.op == OP_iftrue ? OP_iffalse : OP_iftrue), uses);
 
 						else if (a0.op == OP_convert_b)
 							subsume_arg(e, e.op, uses);
 
 						else if (a0.op == OP_greaterequals)
 							subsume_arg(e, (e.op == OP_iftrue ? OP_ifge : OP_ifnge), uses);
 
 						else if (a0.op == OP_greaterthan)
 							subsume_arg(e, (e.op == OP_iftrue ? OP_ifgt : OP_ifngt), uses);
 
 						else if (a0.op == OP_lessthan)
 							subsume_arg(e, (e.op == OP_iftrue ? OP_iflt : OP_ifnlt), uses);
 
 						else if (a0.op == OP_lessequals)
 							subsume_arg(e, (e.op == OP_iftrue ? OP_ifle : OP_ifnle), uses);
 
 						// too aggressive, breaks (at least) zlib
 						//else if (a0.op == OP_equals)
 						//	subsume_arg(e, (e.op == OP_iftrue ? OP_ifeq : OP_ifne), uses);
 
 						else if (a0.op == OP_strictequals)
 							subsume_arg(e, (e.op == OP_iftrue ? OP_ifstricteq : OP_ifstrictne), uses);
 					}
 					break;
 				}
 				
 				case OP_getproperty:
 				{
 					Expr a0 = e.args[0];
 					if (containsOnly(uses.get(a0),e) &&
 						a0.op == OP_findpropstrict &&
 						a0.args.length == 0 &&
 						e.args.length == 1)
 						{
 							e.op = OP_getlex;
 							e.args = noexprs;
 							e.scopes = a0.scopes;
 							a0.setPure();
 							e.flags |= a0.flags&PX | a0.flags&EFFECT;
 						}
 				}
 				break;
 				}
 			}
 		}
 		dce(m);
 	}
 	
 	SetMap<Block,Edge> preds(Deque<Block> code)
 	{
 		SetMap<Block,Edge> pred = new SetMap<Block,Edge>();
 		for (Block b: code)
 			for (Edge s: b.succ())
 				pred.get(s.to).add(s);
 		
 		for (Block b: code)
 			for (Expr e: b)
 				if (e.op != OP_phi)
 					break;
 				else
 				{
 					// make sure each PHI has the same set of incoming
 					// edges as the block does.
 					Set<Edge>phi_in = new TreeSet<Edge>();
 					for (Edge p: e.pred) phi_in.add(p);
 					Set<Edge>blk_in = pred.get(b);
 					assert(phi_in.equals(blk_in));
 				}
 		
 		return pred;
 	}
 
 	SetMap<Block,Edge> allpreds(Deque<Block> code)
 	{
 		SetMap<Block,Edge> pred = new SetMap<Block,Edge>();
 		for (Block b: code)
 		{
 			for (Edge s: b.succ())
 				pred.get(s.to).add(s);
 			for (Edge x: b.xsucc)
 				pred.get(x.to).add(x);
 		}
 		return pred;
 	}
 	
 	int findPhiArg(Expr phi, Edge e)
 	{
 		for (int i=0, n=phi.pred.length; i < n; i++)
 			if (phi.pred[i].equals(e))
 				return i;
 		assert(false);
 		return -1;
 	}
 	
 	/**
 	 * determine if the two blocks have the same exception scope.
 	 * @param b1
 	 * @param b2
 	 * @return
 	 */
 	boolean sameExScope(Block b1, Block b2)
 	{
 		if (b1 == b2) return true;
 		Edge[] xs1 = b1.xsucc;
 		Edge[] xs2 = b2.xsucc;
 		if (xs1.length != xs2.length)
 			return false;
 
 		for (int i=0, n=xs1.length; i < n; i++)
 		{
 			Edge e1 = xs1[i];
 			Edge e2 = xs2[i];
 			if (e1.to != e2.to || e1.handler != e2.handler)
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * control flow optimizations.  
 	 * 
 	 * turn B1->B2 into B1B2 as long as B1's only successor is B2 and B2's only predecessor is B1.
 	 * 
 	 * any branch to a jump can branch to the jump's target
 	 * 
 	 * jump to a return can be replaced by the return
 	 * 
 	 * ISSUE phi's represent copies on an edge.  Need to validate that removing
 	 * edges doesn't remove a required phi-copy.  Any critical edges that have
 	 * been split by previous operations may be removed by this pass.
 	 * 
 	 * Should we only do this pass on non-ssa form?
 	 * @param entry
 	 */
 	boolean cfgopt(Method m)
 	{
 		Deque<Block> code = dfs(m.entry.to);
 		SetMap<Block,Edge> pred = allpreds(code);
 
 		boolean changedout=false;
 		boolean changed;
 		do
 		{
 			changed = false;
 			blocks: for (Block b: code)
 			{
 				Expr last = b.last();
 				Block taken;
 				if (isJump(last) && sameExScope(b, taken=last.succ[0].to))
 				{
 					Edge s = last.succ[0];
 					if (containsOnly(pred.get(taken), s))
 					{
 						// phi nodes can only occur in nodes with more than one predecessor
 						assert(taken.first().op != OP_phi);
 						verboseStatus("STRAIGHTEN "+s);
 						b.remove(last);
 						b.addAll(taken);
 						for (Edge edge: taken.succ())
 							edge.from = b;
 						changed = true;
 						printabc(b, new PrintWriter(System.out));
 						break blocks;
 					}
 					Expr first = taken.first();
 					if (first.op == OP_returnvalue || first.op == OP_returnvoid)
 					{
 						verboseStatus("PRUNE "+b+"->"+s);
 						last.op = first.op;
 						last.args = first.op == OP_returnvoid ? noexprs : new Expr[] { first.args[0] };
 						last.succ = noedges;
 						changed = true;
 						break blocks;
 					}
 					Expr r;
 					if (first.op == OP_phi && taken.size()==2 && (r=taken.last()).op == OP_returnvalue &&
 							r.args[0] == first)
 					{
 						// successor block is returnvalue(phi)
 						verboseStatus("PRUNE "+b+"->"+s);
 						int i = findPhiArg(first, last.succ[0]);
 						last.op = r.op;
 						last.args = new Expr[] { first.args[i] };
 						last.succ = noedges;
 						first.remove(i);
 						changed = true;
 						break blocks;
 					}
 				}
 
 				if (isBranch(last))
 				{
 					Edge out = last.succ[1];
 					Expr cond = last.args[0];
 					taken = out.to;
 					Expr phi, br;
 					if (last.args.length == 1 && taken.size()==2 && (phi=taken.first()).op==OP_phi && 
 							((br=taken.last()).op == OP_iftrue || br.op == OP_iffalse))
 					{
 						int i = findPhiArg(phi,out);
 						if (phi.args[i]==cond)
 						{
 							// cond is in phi contributed by this edge.  so we have a redundant test.
 							// we want to retarget the branch: and adjust any phi nodes that are affected.
 							Edge before = br.op == last.op ? br.succ[1] : br.succ[0];
 							verboseStatus("SKIPTEST old "+out+" new "+before);
 							phi.remove(i);
 							copyTargetPhi(phi, cond, before, out);
 							changed = true;
 							break blocks;
 						}
 					}
 					
 					if (pred.get(last.succ[0].to).size() > 1 && pred.get(taken).size() == 1)
 					{
 						// fall through has multiple preds, taken has only one.  reverse the sense
 						// of the test and reverse the edges.  Note that this doesn't change the
 						// flow so we don't need to recompute preds, etc.
 						invert(last);
 						changed = true;
 						break blocks;
 					}
 				}
 
 				for (Edge edge : last.succ)
 				{
 					if (skip(edge))
 					{
 						changed = true;
 						break blocks;
 					}
 				}
 
 				if (skip(m.entry))
 				{
 					changed = true;
 					break blocks;
 				}
 			}
 			
 			if (changed)
 			{
 				dce(m);
 				code = dfs(m.entry.to);
 				pred = allpreds(code);
 				changedout = true;
 			}
 		}
 		while (changed);
 		return changedout;
 	}
 	
 	boolean skip(Edge edge)
 	{
 		Block to = edge.to;
 		Expr j = to.last();
 		if (to.size()==1 && isJump(j))
 		{
 			// any edge targeting a jump can target the jump's target.
 			verboseStatus("SKIP " + j.succ[0]);
 			copyTarget(j.succ[0], edge);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * reverse the sense of a conditional test and swap the outgoing edges.
 	 * @param br
 	 */
 	void invert(Expr br)
 	{
 		verboseStatus("INVERT "+br);
 
 		switch (br.op)
 		{
 		case OP_iftrue:		br.op = OP_iffalse; break;
 		case OP_iffalse:	br.op = OP_iftrue; break;
 		case OP_ifnlt:		br.op = OP_iflt; break;
 		case OP_ifnle:		br.op = OP_ifle; break;
 		case OP_ifngt:		br.op = OP_ifgt; break;
 		case OP_ifnge:		br.op = OP_ifge; break;
 		case OP_ifeq:		br.op = OP_ifne; break;
 		case OP_ifne:		br.op = OP_ifeq; break;
 		case OP_iflt:		br.op = OP_ifnlt; break;
 		case OP_ifle:		br.op = OP_ifnle; break;
 		case OP_ifgt:		br.op = OP_ifngt; break;
 		case OP_ifge:		br.op = OP_ifnge; break;
 		case OP_ifstricteq:	br.op = OP_ifstrictne; break;
 		case OP_ifstrictne:br.op = OP_ifstricteq; break;
 		}
 		
 		Edge e0 = br.succ[0];
 		Edge e1 = br.succ[1];
 		e0.label = 1;
 		e1.label = 0;
 		br.succ[0] = e1;
 		br.succ[1] = e0;
 	}
 	
 	int ifoper(int op)
 	{
 		switch (op)
 		{
 		default: assert(false); return 0;
 		case OP_iflt:
 		case OP_ifnlt:		return OP_lessthan;
 		case OP_ifle:
 		case OP_ifnle:		return OP_lessequals;
 		case OP_ifgt:
 		case OP_ifngt:		return OP_greaterthan;
 		case OP_ifge:
 		case OP_ifnge:		return OP_greaterequals;
 		case OP_ifne:
 		case OP_ifeq:		return OP_equals;
 		case OP_ifstricteq:
 		case OP_ifstrictne:	return OP_strictequals;
 		}
 	}
 	
 	/**
 	 * for each phi in the target block, add a new phi arg for the new edge,
 	 * contributing the same variable that the old edge contributed. 
 	 * @param succ
 	 * @param oldpred
 	 * @param newpred
 	 */
 	void copyTarget(Edge before, Edge after)
 	{
 		after.to = before.to;
 		for (Expr e: before.to)
 			if (e.op == OP_phi)
 				e.append(e.args[findPhiArg(e,before)], after);
 	}
 	
 	void copyTargetPhi(Expr phi, Expr a, Edge before, Edge after)
 	{
 		after.to = before.to;
 		for (Expr e: before.to)
 		{
 			if (e.op == OP_phi)
 			{
 				Expr phiArg = e.args[findPhiArg(e,before)];
 				e.append(phiArg==phi ? a : phiArg, after);
 			}
 			else
 				break;
 		}
 	}
 	
 	boolean containsOnly(Collection c, Object elem)
 	{
 		return c.size() == 1 && c.contains(elem);
 	}
 		
 	static class Edge implements Comparable<Edge>, Indexable
 	{
 		Block from;
 		Block to;
 		int label;
 		int id;
 		Handler handler; // if this is an exception edge.
 		
 		Edge(Method m, Block f, int i)
 		{
 			from = f;
 			label = i;
 			id = m.edgeId++;
 		}
 		
 		Edge(Method m, Block f, int i, Block t)
 		{
 			this(m,f,i);
 			to = t;
 		}
 		
 		Edge(Method m, Block f, int i, Handler h)
 		{
 			this(m,f,i);
 			handler = h;
 		}
 		
 		public int id()
 		{
 			return id;
 		}
 		
 		public boolean isBackedge()
 		{
 			return from.postorder < to.postorder;
 		}
 		
 		public int hashCode()
 		{
 			return label ^ to.hashCode() ^ (from != null ? from.hashCode() : 0);
 		}
 		
 		public boolean equals(Object o)
 		{
 			return (o instanceof Edge) && ((Edge)o).from == from && ((Edge)o).to == to && ((Edge)o).label == label;
 		}
 		
 		public String toString()
 		{
 			return (isThrowEdge(this) ? handler.toString()+" ":"") + 
 					(from != null ? label+":"+from : "") + "->" + to;
 		}
 		
 		public int compareTo(Edge e)
 		{
 			int d = label - e.label;
 			if (d != 0) return d;
 			if (from != null && e.from == null) return 1;
 			if (from == null && e.from != null) return -1;
 			if (from != null && (d = from.compareTo(e.from)) != 0)
 				return d;
 			return to.compareTo(e.to);
 		}
 	}
 	
 	boolean equiv(Name a, Name b)
 	{
 		return match(a,b) == 0;
 	}
 	
 	boolean equiv(Expr[] a, Expr[] b)
 	{
 		if (a == null || b == null) return false;
 		if (a.length != b.length) return false;
 		for (int i=0; i < a.length; i++)
 			if (!equiv(a[i],b[i]))
 				return false;
 		return true;
 	}
 	
 	boolean equiv(Expr a, Expr b)
 	{
 		if (a == b)
 			return true;
 		
 		if (a != null && b != null && a.op == b.op)
 		{
 			switch (a.op)
 			{
 			case OP_pushtrue:
 			case OP_pushfalse:
 			case OP_pushnan:
 			case OP_pushnull:
 			case OP_pushundefined:
 			case OP_getlocal0:
 			case OP_getlocal1:
 			case OP_getlocal2:
 			case OP_getlocal3:
 				return true;
 			case OP_pushbyte:
 			case OP_pushshort:
 			case OP_pushint:
 			case OP_pushuint:
 			case OP_pushdouble:
 			case OP_pushnamespace:
 			case OP_pushstring:
 				return a.value.equals(b.value);
 			case OP_getlocal:
 			case OP_arg:
 				return a.imm[0] == b.imm[0];
 			case OP_getglobalscope:
 				return a.scopes.length == 0 && b.scopes.length == 0 ||
 					a.scopes.length > 0 && b.scopes.length > 0 && equiv(a.scopes[0], b.scopes[0]);
 			/*case OP_getslot:
 				// TODO unsafe, don't know about intervening stores
 				return a.imm[0] == b.imm[0] && equiv(a.args[0],b.args[0]);
 			case OP_getproperty:
 				// TODO unsafe -- ignores intervening stores & side effects
 				return equiv(a.ref, b.ref) && equiv(a.args,b.args);*/
 			case OP_finddef:
 				return equiv(a.ref, b.ref);
 			}
 		}
 		return false;
 	}
 	
 	void makeCopy(Expr e, Expr a)
 	{
 		// make e be a copy of a
 		assert(e != a);
 		e.op = OP_dup;
 		e.locals = new Expr[] { a };
 		e.scopes = e.args = noexprs;
 		e.setPure();
 	}
 	
 	void makeNop(Expr e)
 	{
 		e.op = OP_nop;
 		e.args = e.locals = e.scopes = noexprs;
 		e.setPure();
 	}
 	
 	Expr dvn_find(Expr e, Block b, Map<Block,Block>idom)
 	{
 		do
 		{
 			for (Expr a: b)
 			{
 				if (a == e) break;
 				if (equiv(a,e))
 					return a;
 			}
 			b = idom.get(b);
 		}
 		while (b != null);
 		return null;
 	}
 	
 	/**
 	 * dominator based value numbering.  This is just LVN plus a search up the
 	 * dominator tree.  When a matching expression is found turn the
 	 * instruction into a copy of the found expression.
 	 * @param code
 	 */
 	void dvn(Method m)
 	{
 		Deque<Block> code = dfs(m.entry.to);
 		Map<Block,Block> idom = idoms(code, preds(code));
 		boolean changed;
 		do
 		{
 			changed = false;
 			for (Block b: code)
 			{
 				for (Expr e: b)
 				{
 					Expr a = dvn_find(e, b, idom);
 					if (a != null)
 					{
 						makeCopy(e,a);
 						changed = true;
 					}
 				}
 			}
 			if (changed)
 				dce(m);
 		}
 		while (changed);
 	}
 	
 	boolean constify(Expr e, Object v)
 	{
 		if (v != null && v != BOTTOM && e.value == null && !hasSideEffect(e))
 		{
 			// expr is const
 			if (v instanceof Integer)
 			{
 				int i = intValue(v);
 				e.op = (i == (byte)i) ? OP_pushbyte :
 					   (i == (short)i) ? OP_pushshort :
 						   OP_pushint;
 			}
 			else if (v instanceof Long)
 			{
 				e.op = OP_pushuint;
 			}
 			else if (v instanceof Number)
 			{
 				double d = doubleValue(v);
 				e.op = Double.isNaN(d) ? OP_pushnan : OP_pushdouble;
 			}
 			else if (v instanceof Boolean)
 			{
 				e.op = v == TRUE ? OP_pushtrue : OP_pushfalse;
 			}
 			else if (v instanceof Namespace)
 			{
 				e.op = OP_pushnamespace;
 			}
 			else if (v == UNDEFINED)
 			{
 				e.op = OP_pushundefined;
 			}
 			else if (v == NULL)
 			{
 				assert(!e.onScope());
 				e.op = OP_pushnull;
 			}
 			else
 			{
 				assert(v instanceof String);
 				e.op = OP_pushstring;
 			}
 			e.pred = noedges;
 			e.args = e.scopes = e.locals = noexprs;
 			e.value = v;
 			return true;
 		}
 		return false;
 	}
 	
 	boolean jumpify(Expr e, Set<Edge> reached)
 	{
 		Edge taken = null;
 		for (Edge s: e.succ)
 		{
 			if (reached.contains(s))
 			{
 				if (taken == null)
 					taken = s;
 				else if (s != taken)
 				{
 					taken = null;
 					break;
 				}
 			}
 		}
 		if (taken != null)
 		{
 			e.op = OP_jump;
 			e.args = noexprs;
 			e.succ = new Edge[] { taken };
 			return true;
 		}
 		return false;
 	}
 	
 	boolean makeConvert(Expr e, Type t, int op, Map<Expr,Typeref> types)
 	{
 		e.op = op;
 		e.args = new Expr[] { e.args[1] };
 		e.flags = flagTable[op];
 		if (type(types,e.args[0]).primitive) 
 			e.setPure();
 		return true;
 	}
 	
 	boolean convertify(Expr e, Binding b0, Map<Expr,Typeref>types)
 	{
 		if (b0.type != null && e.args.length == 2)
 		{
 			if (b0.type.t.itype == NUMBER) 
 				return makeConvert(e,NUMBER,OP_convert_d,types);
 			if (b0.type.t.itype == INT) 
 				return makeConvert(e,INT,OP_convert_i,types);
 			if (b0.type.t.itype == UINT) 
 				return makeConvert(e,UINT,OP_convert_u,types);
 			if (b0.type.t.itype == STRING) 
 				return makeConvert(e,STRING,OP_convert_s,types);
 			if (b0.type.t.itype == BOOLEAN)
 				return makeConvert(e,BOOLEAN,OP_convert_b,types);
 		}
 		return false;
 	}
 	
 	Expr unwrapScope(Expr e, int i)
 	{
 		assert(e.scopes[i].onScope());
 		return e.scopes[i].args[0];
 	}
 	
 	static Type[] copyOf(Type[] in, int newlen)
 	{
 		Type[] out = new Type[newlen];
 		if (newlen > in.length)
 			newlen = in.length;
 		System.arraycopy(in, 0, out, 0, newlen);
 		return out;
 	}
 
 	static Typeref[] copyOf(Typeref[] in, int newlen)
 	{
 		Typeref[] out = new Typeref[newlen];
 		if (newlen > in.length)
 			newlen = in.length;
 		System.arraycopy(in, 0, out, 0, newlen);
 		return out;
 	}
 	
 	static Expr[] copyOf(Expr[] in, int newlen)
 	{
 		Expr[] out = new Expr[newlen];
 		if (newlen > in.length)
 			newlen = in.length;
 		System.arraycopy(in, 0, out, 0, newlen);
 		return out;
 	}
 	
 	static Edge[] copyOf(Edge[] in, int newlen)
 	{
 		Edge[] out = new Edge[newlen];
 		if (newlen > in.length)
 			newlen = in.length;
 		System.arraycopy(in, 0, out, 0, newlen);
 		return out;
 	}
 	
 	/**
 	 * sparse conditional constant propagation
 	 * @param code
 	 */
 	void sccp(Method m)
 	{
 		// first build the SSA Edges.
 		Deque<Block> code = dfs(m.entry.to);
 		EdgeMap<Expr> uses = findUses(code);
 
 		Map<Expr,Object> values = new TreeMap<Expr,Object>();
 		Map<Expr,Typeref> types = new TreeMap<Expr,Typeref>();
 		Set<Edge> reached = new TreeSet<Edge>();
 		
 		sccp_analyze(m, uses, values, types, reached);
 
 		verboseStatus("REACHED " + reached);
 		verboseStatus("TYPES " + types);
 	
 		sccp_cfgopt(values, types, reached);
 		
 		// now the CFG is cleaned up
 		dce(m);
 		
 		// now do type/value based peephole work on each expr in DFS order
 		code = dfs(m.entry.to);
 		uses = findUses(code);
 		
 		// modify the code based on analysis:
 		// * capture scope types for OP_newclass & OP_newfunction
 		// * turn expressions into constants
 		
 		WorkSet<Expr> work = new WorkSet<Expr>();
 		for (Block b: code)
 			for (Expr e: b)
 				work.add(e);
 
 		while (!work.isEmpty())
 		{
 			Expr e = remove(work);
 			sccp_modify(m, uses, values, types, e, work);
 		}
 		
 		dce(m);
 		//verboseStatus("after sccp");
 		//print(dfs(m.entry.to));
 	}
 
 	void sccp_cfgopt(Map<Expr, Object> values, Map<Expr,Typeref> types, Set<Edge> reached)
 	{
 		Set<Block> blocks = new WorkSet<Block>();
 		for (Edge e: reached)
 			blocks.add(e.to);
 
 		for (Block b: blocks)
 		{
 			// clean up phis
 			for (Expr e: b)
 			{
 				if (e.op == OP_phi)
 				{
 					for (int j=e.pred.length-1; j >= 0; j--)
 					{
 						Edge p = e.pred[j];
 						if (!reached.contains(p))
 							e.remove(j);
 					}
 				}
 				else if (e.succ != null)
 				{
 					if (e.succ.length > 1 && !jumpify(e, reached) && e.op == OP_lookupswitch)
 					{
 						Expr phi;
 						if (b.size()==2 && (phi=b.first()).op == OP_phi && e.args[0]==phi)
 						{
 							// block is switch(phi)
 							// TODO what about a switch with only some branches not taken?
 							for (int i=phi.args.length-1; i >= 0; i--)
 							{
 								Object v = values.get(phi.args[i]);
 								if (v instanceof Number)
 								{
 									int j = intValue(v);
 									Edge in = phi.pred[i];
 									Edge out = e.succ[j];
 									copyTarget(out, in);
 									phi.remove(i);
 								}
 							}
 						}
 					}
 				}
 				else if (e.isOper() && e.onStack())
 				{
 					boolean pure = true;
 					for (Expr a: e.args) if (!type(types,a).primitive) pure = false;
 					for (Expr a: e.locals) if (!type(types,a).primitive) pure = false;
 					if (pure)
 						e.setPure();
 					constify(e, values.get(e));
 				}
 			}
 		}
 	}
 	
 	boolean subsume_arg(Expr e, int op, EdgeMap<Expr>uses)
 	{
 		e.op = op;
 		Expr a = e.args[0];
 		e.flags |= a.flags & (PX|EFFECT);
 		a.setPure(); // so it becomes dead and is removed.
 		uses.get(a).remove(e);
 		e.args = copyOf(a.args, a.args.length);
 		for (Expr x: e.args)
 			uses.get(x).add(e);
 		return true;
 	}
 
 	boolean canEarlyBindMethod(Method m, Binding b)
 	{
 		return true && m.abc.mergedAbcs.contains(b.abc);
 	}
 	
 	boolean canEarlyBindSlot(Method m, Binding b)
 	{
 		return b.slot != 0 && m.abc.mergedAbcs.contains(b.abc);
 	}
 
 	void sccp_modify(Method m, EdgeMap<Expr> uses, Map<Expr, Object> values, Map<Expr, Typeref> types, 
 			Expr e, WorkSet<Expr> work)
 	{
 		sccp_rename(uses, e, e.args);
 		sccp_rename(uses, e, e.locals);
 		boolean changed;
 		do
 		{
 			changed = false;
 			switch (e.op)
 			{
 			case OP_newclass:
 			{
 				// capture scope chain
 				Type c = e.c;
 				c.scopes = copyOf(m.cx.scopes, m.cx.scopes.length+e.scopes.length);
 				int i = m.cx.scopes.length;
 				for (Expr s: e.scopes)
 					c.scopes[i++] = types.get(s);
 				readyType(c);
 				
 				Type t = c.itype;
 				t.scopes = copyOf(c.scopes, c.scopes.length+1);
 				t.scopes[c.scopes.length] = c.ref.nonnull();
 				readyType(t);
 				break;
 			}
 			case OP_newfunction:
 			{
 				Method f = e.m;
 				// TODO makeIntoPrototypeFunction()
 				Type t = new Type(m.name, FUNCTION);
 				t.scopes = copyOf(m.cx.scopes, m.cx.scopes.length+e.scopes.length);
 				int i = m.cx.scopes.length;
 				for (Expr s: e.scopes)
 					t.scopes[i++] = types.get(s);
 				f.cx = t;
 				readyMethod(f);
 				break;
 			}
 			case OP_returnvalue:
 				if (type(types,e.args[0]) == VOID)
 				{
 					e.op = OP_returnvoid;
 					e.args = noexprs;
 				}
 				else
 				{
 					Type t0 = type(types,e.args[0]);
 					if (istype(t0, m.returns.t))
 					{
 						Expr a0 = e.args[0];
 						if (m.returns.t == INT && a0.op == OP_convert_i)
 						{
 							uses.get(a0).remove(e);
 							a0 = e.args[0] = a0.args[0];
 							uses.get(a0).add(e);
 						}
 					}
 				}
 				break;
 	
 			case OP_getglobalscope:
 			{
 				if (m.cx.scopes.length == 0)
 				{
 					// global is inner scope 0, so copy that
 					makeCopy(e, unwrapScope(e, 0));
 					changed = true;
 				}
 				break;
 			}
 			
 			case OP_getscopeobject:
 			{
 				makeCopy(e, unwrapScope(e, 0));
 				changed = true;
 				break;
 			}
 			
 			case OP_istypelate:
 			{
 				Type t1 = type(types,e.args[1]);
 				if (t1.itype != null && namedTypes.contains(t1.itype.name))
 				{
 					e.op = OP_istype;
 					e.ref = t1.itype.name;
 					e.args = new Expr[] { e.args[0] };
 					e.clearPx();
 					changed = true;
 				}
 				break;
 			}
 			
 			case OP_istype:
 			{
 				if (namedTypes.contains(e.ref))
 					e.clearPx();
 				break;
 			}
 			
 			case OP_pushscope:
 			case OP_pushwith:
 			{
 				if (!types.get(e.args[0]).nullable)
 					e.clearPx();
 				break;
 			}
 			
 			case OP_coerce:
 			{
 				Expr a0 = e.args[0];
 				Typeref t = types.get(e);
 				Typeref t0 = types.get(a0);
 				if (t == t0)
 				{
 					// upcast
 					makeCopy(e, a0);
 					changed = true;
 				}
 				else
 				{
 					Object v0 = values.get(a0);
 					if (v0 == NULL && t0.nullable && t0.t != VOID)
 					{
 						makeCopy(e, a0);
 						changed = true;
 					}
 					else
 					{
 						if (namedTypes.get(e.ref) == OBJECT)
 						{
 							e.op = OP_coerce_o;
 							e.clearEffect();
 							e.ref = null;
 							e.imm = null;
 							changed = true;
 						}
 					}
 				}
 				break;
 			}
 			
 			// turn findprop into either getglobalscope, getscopeobject, or finddef to expose redundancies
 			case OP_findpropstrict:
 			case OP_findproperty:
 			{
 				int i = findInner(e.ref, e.scopes, types);
 				if (i >= 0)
 				{
 					makeCopy(e, unwrapScope(e,i));
 					for (Expr s: e.scopes)
 						uses.get(s).remove(e);
 					changed = true;
 				}
 				else if ((i = findOuter(e.ref, m.cx.scopes)) >= 0)
 				{
 					if (i == 0) // getglobalscope
 					{
 						e.op = OP_getglobalscope;
 						e.scopes = noexprs;
 						e.setPure();
 						for (Expr s: e.scopes)
 							uses.get(s).remove(e);
 						changed = true;
 					}
 					else
 					{
 						// can't change the opcode
 						e.setPure();
 						// TODO - early bind e.ref
 					}
 				}
 				else if (globals.contains(e.ref))
 				{
 					e.op = OP_finddef;
 					e.flags = flagTable[e.op];
 					e.scopes = noexprs;
 					e.ref = globals.getName(e.ref);
 					for (Expr s: e.scopes)
 						uses.get(s).remove(e);
 					changed = true;
 				}
 				else if (e.op == OP_findproperty)
 				{
 					// will return global if nothing found.
 					if (m.cx.scopes.length == 0)
 					{
 						makeCopy(e, unwrapScope(e,0));
 						for (Expr s: e.scopes)
 							uses.get(s).remove(e);
 						changed = true;
 					}
 					else
 					{
 						e.op = OP_getglobalscope;
 						for (Expr s: e.scopes)
 							uses.get(s).remove(e);
 						e.scopes = noexprs;
 						e.setPure();
 						changed = true;
 					}
 				}
 				break;
 			}
 			
 			// getsuper should look in base of enclosing type.
 			// if either bind to a final getter, use callstatic.
 			
 			case OP_getproperty:
 			case OP_getsuper:
 			{
 				Typeref t0 = types.get(e.args[0]);
 				Binding bind = t0.findGet(e.ref);
 				if (isSlot(bind))
 				{
 					e.clearEffect();
 					if (!t0.nullable)
 						e.clearPx();
 					e.ref = bind.name;
 					if (canEarlyBindSlot(m, bind))
 					{
 						if (!isConst(bind) || !constify(e, values.get(e)))
 						{
 							e.op = OP_getslot;
 							e.imm = new int[] { bind.slot };
 							changed = true;
 						}
 					}
 				}
 				else if (bind != null)
 				{
 					// narrow the binding
 					e.ref = bind.name;
 				}
 				break;
 			}
 			
 			case OP_initproperty:
 			{
 				Type t0 = type(types,e.args[0]);
 				Object v1 = values.get(e.args[1]);
 				Binding bind = t0.find(e.ref);
 				if (bind != null)
 				{
 					// narrow the binding
 					e.ref = bind.name;
 					// isSlot() is to aggressive, breaks (at least) zlib
 					if (isConst(bind) && bind.value != null && bind.value.equals(v1))
 					{
 						// initializing a slot to it's known default value?
 						// TODO ensure no intervening writes to non-const values
 						makeNop(e);
 						for (Expr a: e.args) 
 							uses.get(a).remove(e);
 					}	
 					else if (isSlot(bind) && canEarlyBindSlot(m, bind))
 					{	
 						e.op = OP_setslot;
 						e.imm = new int[] { bind.slot };
 						changed = true;
 					}
 				}
 				break;
 			}
 			
 			case OP_setproperty:
 			case OP_setsuper:
 			{
 				Type t0 = type(types,e.args[0]);
 				Binding bind = t0.find(e.ref);
 				if (isSlot(bind))
 				{
 					e.ref = bind.name;
 					if (canEarlyBindSlot(m, bind))
 					{
 						e.op = OP_setslot;
 						e.imm = new int[] { bind.slot };
 						changed = true;
 					}
 				}
 				else if (bind != null)
 				{
 					// narrow the binding
 					e.ref = bind.name;
 				}
 				break;
 			}
 			
 			case OP_callproperty:
 			case OP_callproplex:
 			case OP_callsuper:
 			case OP_constructprop:
 			case OP_callpropvoid:
 			{
 				// TODO turn callproplex into callproperty if binding is method
 				Type t0 = type(types,e.args[0]);
 				Binding b0 = t0.findGet(e.ref);
 				if (b0 != null)
 				{
 					// narrow the binding
 					e.ref = b0.name;
 					if (e.op == OP_callproperty && isMethod(b0))
 					{
 						if (t0.primitive && e.args.length==1 && e.ref.equals(AS3_TOSTRING) && type(types,e) == STRING)
 						{
 							// TODO this may open up further reductions.  how to iterate?
 							// or, need to add similar logic in sccp_eval
 							e.op = OP_convert_s;
 							e.setPure();
 							changed=true;
 						}
 						else if (canEarlyBindMethod(m,b0) && (t0.isFinal() || b0.isFinal()))
 						{
 							e.op = OP_callstatic;
 							e.m = b0.method;
 							changed=true;
 						}
 						else if (USE_CALLMETHOD && canEarlyBindMethod(m,b0))
 						{
 							e.op = OP_callmethod;
 							e.imm = new int[] { b0.slot };
 							changed = true;
 						}
 					}
 					else if (e.op == OP_callproperty && isClass(b0))
 					{
 						changed |= convertify(e, b0, types);
 					}
 				}
 				if (uses.get(e).isEmpty())
 				{
 					if (e.op == OP_callsuper) 
 						e.op = OP_callsupervoid;
 					if (e.op == OP_callproperty)
 						e.op = OP_callpropvoid;
 				}
 				break;
 			}
 			
 			case OP_convert_u:
 				if (e.args[0].op == OP_convert_i)
 					e.args[0] = e.args[0].args[0];
 				// fall through
 			case OP_coerce_s:
 			case OP_convert_s:
 			case OP_convert_d:
 			case OP_coerce_o:
 			case OP_convert_b:
 			case OP_convert_i:
 			case OP_coerce_a:
 			{
 				Expr a0 = e.args[0];
 				Type t = type(types,e);
 				Type t0 = type(types,e.args[0]);
 				if (t == t0)
 				{
 					makeCopy(e,e.args[0]);
 					changed=true;
 				}
 				else if (e.op == OP_convert_i)
 				{
 					if (a0.op == OP_negate)
 					{
 						// convert_i(negate(x)) => negate_i(x)
 						e.op = OP_negate_i;
 						e.args = new Expr[] { a0.args[0] };
 						changed=true;
 					}
 					else if (a0.op == OP_decrement)
 					{
 						e.op = OP_decrement_i;
 						e.args = new Expr[] { a0.args[0] };
 						changed = true;
 					}
 				}
 				break;
 			}
 			
 			case OP_subtract:
 			{
 				Object v1 = values.get(e.args[1]);
 				if (doubleValue(v1) == 1)
 				{
 					e.args = new Expr[] { e.args[0] };
 					e.op = OP_decrement;
 					changed = true;
 				}
 				else if (doubleValue(v1) == -1)
 				{
 					e.args = new Expr[] { e.args[0] };
 					e.op = OP_increment;
 					changed = true;
 				}
 				break;
 			}
 			
 			case OP_add:
 			{
 				Type t = type(types,e);
 				Object v0 = values.get(e.args[0]);
 				Object v1 = values.get(e.args[1]);
 				if (t.numeric)
 				{
 					if (doubleValue(v0)==1)
 					{
 						e.args = new Expr[] { e.args[1] };
 						e.op = OP_increment;
 						changed = true;
 					}
 					else if (doubleValue(v1)==1)
 					{
 						e.args = new Expr[] { e.args[0] };
 						e.op = OP_increment;
 						changed = true;
 					}
 					else if (doubleValue(v0) == -1)
 					{
 						e.args = new Expr[] { e.args[1] };
 						e.op = OP_decrement;
 						changed = true;
 					}
 					else if (doubleValue(v1) == -1)
 					{
 						e.args = new Expr[] { e.args[0] };
 						e.op = OP_decrement;
 						changed = true;
 					}
 				}
 				else if (t == STRING)
 				{
 					if ("".equals(v0))
 					{
 						e.args = new Expr[] { e.args[1] };
 						e.op = OP_convert_s;
 						changed = true;
 					}
 					else if ("".equals(v1))
 					{
 						e.args = new Expr[] { e.args[0] };
 						e.op = OP_convert_s;
 						changed = true;
 					}
 				}
 			}
 			}
 		
 			if (changed)
 				work.addAll(uses.get(e));
 		}
 		while (changed);
 	}
 
 	void sccp_rename(EdgeMap<Expr> uses, Expr e, Expr[]args)
 	{
 		for (int i=args.length-1; i >= 0; i--)
 		{
 			Expr a = args[i];
 			if (a.op == OP_dup)
 			{
 				uses.get(a).remove(e);
 				a = args[i] = a.locals[0];
 				uses.get(a).add(e);
 			}
 		}
 	}
 	
 	Map<Expr,Typeref> verify_types(Method m, Deque<Block> code, Map<Block,Block> idom)
 	{
 		EdgeMap<Expr> uses = findUses(code);
 		Map<Expr,Typeref> types = new TreeMap<Expr,Typeref>();
 		Set<Expr> work = new WorkSet<Expr>();
 		for (Block b: code)
 			work.addAll(b.exprs);
 		do
 		{
 			Expr e = remove(work);
 			if (e.onStack() || e.inLocal() || e.onScope() || e.op==OP_phi)
 			{
 				Typeref tref = verify_eval(m, e, types, idom);
 				if (!tref.equals(types.get(e)))
 				{
 					types.put(e, tref);
 					work.addAll(uses.get(e));
 				}
 			}
 		}
 		while (!work.isEmpty());
 		return types;
 	}
 
 	void sccp_analyze(Method m, EdgeMap<Expr> uses, Map<Expr, Object> values, Map<Expr, Typeref> types, Set<Edge> reached)
 	{
 		Set<Edge> flowWork = new WorkSet<Edge>();
 		Set<Expr> ssaWork = new WorkSet<Expr>();
 		Set<Expr> ready = new WorkSet<Expr>();
 
 		flowWork.add(m.entry);
 		do
 		{
 			while (!flowWork.isEmpty())
 			{
 				Edge edge = remove(flowWork);
 				if (!reached.contains(edge))
 				{
 					reached.add(edge);
 					Block b = edge.to;
 					ready.addAll(b.exprs);
 					ssaWork.addAll(b.exprs);
 					for (Edge x: b.xsucc)
 						flowWork.add(x);
 				}
 			}
 			while (!ssaWork.isEmpty())
 			{
 				Expr e = remove(ssaWork);
 				if (ready.contains(e))
 					sccp_eval(m, e, values, types, flowWork, ssaWork, uses);
 			}
 		}
 		while (!flowWork.isEmpty());
 	}
 	
 	boolean isCopy(Expr e, Map<Expr,Type>types, Set<Expr>upcasts)
 	{
 		Type t = types.get(e);
 		Type t0 = types.get(e.args[0]);
 		return t == t0 || isUpcast(e,types) && !upcasts.contains(e);
 	}
 	
 	boolean isUpcast(Expr e, Map<Expr,Type>types)
 	{
 		if (e.isCoerce())
 		{
 			Type t0 = types.get(e.args[0]);
 			Type t = types.get(e);
 			return t != t0 && istype(t0, t);
 		}
 		return false;
 	}
 
 	void insert_casts(Method m)
 	{
 		Deque<Block> code = dfs(m.entry.to);
 		SetMap<Block,Edge> pred = preds(code);
 		Map<Block,Block> idom = idoms(code, pred);
 		Map<Expr,Typeref> types = verify_types(m, code, idom);
 		
 		// TODO this fixes things by inserting upcasts only.  We could use
 		// the types we know from sccp_analyze and insert casts that tell
 		// the verifier what we know that it can't figure out on its own.
 		// this can be done expression by expression rather than looking
 		// at phi nodes & splitting critical edges.
 		
 		// it would also be a good sanity check.  If the verifier deduces a
 		// more specific type than we have, then we have a bug in either the
 		// verifier or the optimizer.
 
 		for (Block b: code)
 		{
 			for (Expr e: b)
 			{
 				if (e.op != OP_phi)
 					break;
 				Typeref etype = types.get(e);  // merged type, possibly mdb of arg types
 				for (int i=e.args.length-1; i >= 0; i--)
 				{
 					Expr a = e.args[i];
 					Edge p = e.pred[i];
 					Typeref atype = types.get(a);
 					if (isLoop(p, idom) ? !etype.equals(atype) : isAtom(etype.t) != isAtom(atype.t))
 					{
 						// skip String->String?
 						if((etype.t == atype.t) && etype.nullable)
 							continue;
 						
 						verboseStatus("MISSING CAST " + a + " " + atype+"->"+etype+" on " + p);
 						if (isCritical(p,pred))
 						{
 							split(p, m, pred);
 							p = e.pred[i];
 						}
 						// TODO best to insert this right after a?
 						Expr upcast = upcast(a, m, etype.t);
 						append(p, upcast);
 						e.args[i] = upcast;
 					}
 				}
 			}
 		}
 		
 		verboseStatus("VERIFY TYPES "+types);
 	}
 	
 	Expr upcast(Expr a, Method m, Type t)
 	{
 		if (t == ANY)
 		{
 			return new Expr(m, OP_coerce_a, a);
 		}
 		else if (t == OBJECT)
 		{
 			return new Expr(m, OP_coerce_o, a);
 		}
 
 		return new Expr(m, OP_coerce, t.name, a);
 	}
 	
 	boolean isAtom(Type t)
 	{
 		return t.atom;
 	}
 	
 	boolean isPointer(Type t)
 	{
 		return !isAtom(t) && !t.numeric;
 	}
 	
 	Object eval_convert_i(Object v0)
 	{
 		return v0 instanceof Number ? intValue(v0) :
 			   v0 == TRUE ? 1 :
 			   v0 == FALSE ? 0 :
 			   BOTTOM;
 	}
 
 	Object eval_convert_u(Object v0)
 	{
 		return v0 instanceof Number ? uintValue(v0) :
 			   v0 == TRUE ? 1 :
 			   v0 == FALSE ? 0 :
 			   BOTTOM;
 	}
 
 	Object eval_convert_d(Object v0)
 	{
 		return v0 instanceof Number ? doubleValue(v0) :
 			   v0 == TRUE ? 1 :
 			   v0 == FALSE ? 0 :
 			   BOTTOM;
 	}
 
 	Object eval_convert_b(Object v0)
 	{
 		return v0 == BOTTOM ? BOTTOM :
 			   booleanValue(v0) ? TRUE : FALSE;
 	}
 	
 	Object eval_convert_s(Object v0)
 	{
 		return v0 != BOTTOM ? stringValue(v0) : BOTTOM;
 	}
 	
 	Typeref eval_coerce_s(Typeref t0)
 	{
 		if (t0.nullable)
 			return t0.t == VOID || t0.t == NULL ? NULL.ref : STRING.ref;
 		else
 			return STRING.ref.nonnull();
 	}
 	
 	Object eval_coerce_s(Object v0)
 	{
 		return v0 == UNDEFINED || v0 == NULL ? NULL :
 			   v0 != BOTTOM ? stringValue(v0) :
 			   BOTTOM;
 	}
 	
 	Typeref eval_coerce_o(Typeref t0)
 	{
 		if (t0.nullable)
 			return istype(t0.t, OBJECT) ? t0 :
 			   t0.t == VOID || t0.t == NULL ? NULL.ref :
 			   OBJECT.ref;
 		else
 			return istype(t0.t, OBJECT) ? t0 : OBJECT.ref.nonnull();
 	}
 	
 	Object eval_coerce_o(Object v0, Type t0)
 	{
 		return istype(t0, OBJECT) ? v0 :
 			   t0 == VOID || t0 == NULL ? NULL :
 			   BOTTOM;
 	}
 	
 	boolean lessthan(Object v0, Object v1)
 	{
 		if (v0 instanceof String && v1 instanceof String)
 		{
 			return ((String)v0).compareTo((String)v1) < 0;
 		}
 		else
 		{
 			return doubleValue(v0) < doubleValue(v1);
 		}
 	}
 	
 	Type type(Map<Expr,Typeref>types, Expr e)
 	{
 		return types.get(e).t;
 	}
 	
 	boolean nullable(Expr e, Map<Expr,Typeref>types)
 	{
 		return types.get(e).nullable;
 	}
 
 	/**
 	 * visit a single expression.  compute it's type and constant value.  If
 	 * either change, add any dependents to the appropriate work list.
 	 * 
 	 * @param m
 	 * @param e
 	 * @param values
 	 * @param types
 	 * @param flowWork
 	 * @param ssaWork
 	 * @param uses
 	 */
 	void sccp_eval(Method m, Expr e, 
 			Map<Expr,Object> values, 
 			Map<Expr,Typeref> types,
 			Set<Edge>flowWork, Set<Expr>ssaWork, 
 			EdgeMap<Expr> uses)
 	{
 		Object v = null;
 		Typeref tref = null;
 		
 		if (e.op == OP_phi)
 		{
 			for (Expr a: e.args)
 			{
 				// compute the phi() meet operation.  ignore any inputs we
 				// haven't evaluated yet.  If all inputs have the same value,
 				// phi() has that value.  Otherwise return BOTTOM.
 				Object av = values.get(a);
 				if (av == null) continue; // ignore TOP inputs
 				
 				if (v == null)
 					v = av;
 				else if (!av.equals(v))
 					v = BOTTOM;
 				
 				// same idea for types, but if they aren't all the same
 				// then compute the most derived base class (mdb) of the types.
 				Typeref aref = types.get(a);
 				if (tref == null)
 					tref = aref;
 				else if (!tref.equals(aref))
 					tref = mdb(tref,aref);
 			}
 		}
 		else
 		{
 			// if any arg is TOP result is TOP (unchanged)
 			for (Expr a : e.args) if (!values.containsKey(a))	return;
 			for (Expr a : e.scopes) if (!values.containsKey(a))	return;
 			for (Expr a : e.locals) if (!values.containsKey(a))	return;
 			
 			v = BOTTOM;
 			tref = ANY.ref;
 			
 			switch (e.op)
 			{
 			default:
 				System.err.println("unhandled op:" + e.op + ":"+ opNames[e.op]);
 				assert(false);
 			
 			case OP_hasnext2_o:
 			case OP_nextname:
 			case OP_nextvalue:
 			case OP_call:
 			case OP_callsuper:
 			case OP_getsuper:
 			case OP_getdescendants:
 				break;
 				
 			case OP_convert_o:
 			{
 				tref = types.get(e.args[0]).nonnull();
 				v = values.get(e.args[0]);
 				break;
 			}	
 			case OP_esc_xattr:
 			case OP_esc_xelem:
 				tref = STRING.ref.nonnull();
 				break;
 				
 			case OP_newcatch:
 				tref = m.handlers[e.imm[0]].activation;
 				break;
 				
 			case OP_newobject:
 				tref = OBJECT.ref.nonnull();
 				break;
 			
 			case OP_newarray:
 				tref = ARRAY.ref.nonnull();
 				break;
 				
 			case OP_newactivation:
 				tref = m.activation;
 				break;
 				
 			case OP_getglobalscope:
 				if (m.cx.scopes.length > 0)
 				{
 					tref = m.cx.scopes[0];
 				}
 				else
 				{
 					// same as getscopeobject<0>
 					v = values.get(e.scopes[0].args[0]);
 					tref = types.get(e.scopes[0].args[0]);
 				}
 				break;
 				
 			case OP_getscopeobject:
 				v = values.get(e.scopes[0].args[0]);
 				tref = types.get(e.scopes[0].args[0]);
 				break;
 				
 			case OP_newclass:
 				tref = e.c.ref.nonnull();
 				break;
 				
 			case OP_newfunction:
 				tref = FUNCTION.ref.nonnull();
 				break;
 				
 			case OP_finddef:
 				if (globals.contains(e.ref))
 					tref = globals.get(e.ref);
 				break;
 				
 			case OP_findpropstrict:
 			case OP_findproperty:
 			{
 				int i = findInner(e.ref, e.scopes, types);
 				if (i >= 0)
 				{
 					v = values.get(e.scopes[i]);
 					tref = types.get(e.scopes[i]);
 				}
 				else if ((i = findOuter(e.ref, m.cx.scopes)) >= 0)
 				{
 					tref = m.cx.scopes[i];
 				}
 				else if (globals.contains(e.ref))
 				{
 					tref = globals.get(e.ref);
 				}
 				else
 				{
 					// not found.  use global.
 					if (m.cx.scopes.length > 0)
 					{
 						tref = m.cx.scopes[0];
 					}
 					else
 					{
 						v = values.get(e.scopes[0]);
 						tref = types.get(e.scopes[0]);
 					}
 				}
 				break;
 			}
 			
 			case OP_getlex:
 			{
 				// findproperty + getproperty
 				int i = findInner(e.ref, e.scopes, types);
 				Typeref stref = i >= 0 ? types.get(e.scopes[i]) : 
 					(i=findOuter(e.ref, m.cx.scopes)) >= 0 ? m.cx.scopes[i] :
 						globals.contains(e.ref) ? globals.get(e.ref) :
 							m.cx.scopes.length > 0 ? m.cx.scopes[0] :
 								types.get(e.scopes[0]);
 	
 				Binding b = stref.t.findGet(e.ref);
 				// code below is a copy of OP_getproperty
 				if (isSlot(b))
 				{
 					// TODO we only compute const value here if primitive type.
 					// it would be more correct if we knew whether the initializer
 					// changed the const value.  (consts can be computed in init).
 					tref = b.type;
 					if (isConst(b) && defaultValueChanged(b))
 						v = b.value;
 				}
 				else if (isMethod(b))
 				{
 					// TODO if base type is or might be XML, return ANY
 					// TODO use MethodClosure, more specific than Function
 					tref = FUNCTION.ref.nonnull();
 				}
 				else if (isGetter(b))
 				{
 					tref = b.method.returns;
 				}
 				break;
 			}
 			
 			case OP_construct:
 			{
 				tref = OBJECT.ref.nonnull();
 				break;
 			}
 			
 			case OP_constructprop:
 			{
 				Type ot = type(types,e.args[0]); // type of base object
 				Binding b = ot.findGet(e.ref);
 				if (b != null && b.type != null && b.type.t.itype != null)
 				{
 					tref = b.type.t.itype.ref.nonnull();
 					break;
 				}
 				break;
 			}
 			
 			case OP_callproperty:
 			case OP_callproplex:
 			{
 				Type ot = type(types, e.args[0]);
 				Binding b = ot.findGet(e.ref);
 				if (isMethod(b))
 				{
 					tref = b.method.returns;
 				}
 				else if (isSlot(b) && b.type != null)
 				{
 					// each of these has same logic as convert_i, convert_s, etc
 					if (b.type.t.itype == INT) 
 					{
 						tref = INT.ref;
 						v = eval_convert_i(values.get(e.args[1]));
 					}
 					else if (b.type.t.itype == UINT) 
 					{
 						tref = UINT.ref;
 						v = eval_convert_u(values.get(e.args[1]));
 					}
 					else if (b.type.t.itype == STRING)
 					{
 						tref = STRING.ref.nonnull();
 						v = eval_convert_s(values.get(e.args[1]));
 					}
 					else if (b.type.t.itype == BOOLEAN)
 					{
 						tref = BOOLEAN.ref;
 						v = eval_convert_b(values.get(e.args[1]));
 					}
 					else if (b.type.t.itype == NUMBER)
 					{
 						tref = NUMBER.ref;
 						v = eval_convert_d(values.get(e.args[1]));
 					}
 				}
 				break;
 			}
 							
 			case OP_applytype:
 				tref = types.get(e.args[0]).nonnull();
 				break;
 			
 			case OP_callstatic:
 				tref = e.m.returns;
 				break;
 			
 			case OP_arg:
 				if (e.imm[0] < m.params.length)
 					tref = m.params[e.imm[0]];
 				else if (m.needsArguments()||m.needsRest() && e.imm[0] == m.params.length)
 					tref = ARRAY.ref.nonnull();
 				else
 					tref = VOID.ref;
 				break;
 				
 			case OP_xarg:
 				tref = m.handlers[e.imm[0]].type;
 				break;
 				
 			case OP_getslot:
 			{
 				Type t0 = type(types, e.args[0]);
 				Binding b = t0.findSlot(e.imm[0]);
 				if (b != null)
 					tref = b.type;
 				break;
 			}
 			
 			case OP_getproperty:
 			{
 				Type t0 = type(types, e.args[0]);
 				Binding b = t0.findGet(e.ref);
 				if (isSlot(b))
 				{
 					// TODO we only compute const value here if primitive type.
 					// it would be more correct if we knew whether the initializer
 					// changed the const value.  (consts can be computed in init).
 					tref = b.type;
 					if (isConst(b) && defaultValueChanged(b))
 						v = b.value;
 				}
 				else if (isMethod(b))
 				{
 					// TODO if base type is or might be XML, return ANY
 					// TODO use MethodClosure, more specific than Function
 					tref = FUNCTION.ref.nonnull();
 				}
 				else if (isGetter(b))
 				{
 					tref = b.method.returns;
 				}
 				break;
 			}
 			
 			case OP_pushundefined:
 				v = e.value;
 				tref = VOID.ref;
 				break;
 				
 			case OP_pushnull:
 				v = e.value;
 				tref = NULL.ref;
 				break;
 				
 			case OP_pushtrue:
 			case OP_pushfalse:
 				v = e.value;
 				tref = BOOLEAN.ref;
 				break;
 				
 			case OP_pushbyte:
 			case OP_pushshort:
 			case OP_pushint:
 				v = e.value;
 				tref = INT.ref;
 				break;
 				
 			case OP_pushuint:
 				v = e.value;
 				tref = UINT.ref;
 				break;
 				
 			case OP_pushstring:
 				v = e.value;
 				tref = STRING.ref.nonnull();
 				break;
 				
 			case OP_pushnan:
 			case OP_pushdouble:
 				v = e.value;
 				tref = NUMBER.ref;
 				break;
 				
 			case OP_pushnamespace:
 				v = e.value;
 				tref = NAMESPACE.ref.nonnull();
 				break;
 				
 			case OP_jump:
 				flowWork.add(e.succ[0]);
 				return;
 	
 			case OP_lookupswitch:
 			{
 				Object v1 = values.get(e.args[0]);
 				if (v1 == BOTTOM)
 					for (Edge s: e.succ)
 						flowWork.add(s);
 				else
 				{
 					// input is const
 					int i = intValue(v1);
 					if (i < 0 || i >= e.succ.length-1) 
 						i = e.succ.length-1;
 					flowWork.add(e.succ[i]);
 				}
 				return;
 			}
 			
 			case OP_iffalse:
 			case OP_iftrue:
 			{
 				Object v1 = values.get(e.args[0]);
 				if (v1 == BOTTOM)
 				{
 					flowWork.add(e.succ[0]);
 					flowWork.add(e.succ[1]);
 				}
 				else if (e.op == OP_iffalse)
 					flowWork.add(e.succ[booleanValue(v1) ? 0 : 1]);
 				else if (e.op == OP_iftrue)
 					flowWork.add(e.succ[booleanValue(v1) ? 1 : 0]);
 				return;
 			}
 			
 			case OP_pushscope:
 			case OP_pushwith:
 				// treat this as a copy.
 				v = values.get(e.args[0]);
 				tref = types.get(e.args[0]).nonnull();
 				break;
 				
 			case OP_convert_b:
 				tref = BOOLEAN.ref;
 				v = eval_convert_b(values.get(e.args[0]));
 				break;
 				
 			case OP_not:
 			{
 				tref = BOOLEAN.ref;
 				Object v0 = values.get(e.args[0]);
 				if (v0 != BOTTOM)
 					v = booleanValue(v0) ? FALSE : TRUE;
 				break;
 			}
 				
 			case OP_deleteproperty:
 				// TODO result is const false for any declared property
 			case OP_deldescendants:
 			case OP_hasnext:
 			case OP_hasnext2:
 			case OP_equals:
 			case OP_strictequals:
 			case OP_in:
 			case OP_istype:
 			case OP_istypelate:
 			case OP_instanceof:
 				tref = BOOLEAN.ref;
 				break;
 				
 			case OP_lessthan:
 			case OP_lessequals:
 			case OP_greaterthan:
 			case OP_greaterequals:
 			{
 				tref = BOOLEAN.ref;
 				Object v0 = values.get(e.args[0]);
 				Object v1 = values.get(e.args[1]);
 				if (v0.equals(NAN) || v0 == UNDEFINED || v1.equals(NAN) || v1 == UNDEFINED)
 					v = FALSE;
 				else if (v0 != BOTTOM && v1 != BOTTOM)
 					v = e.op == OP_lessthan ?     lessthan(v0,v1) :
 						e.op == OP_lessequals ?  !lessthan(v1,v0) :
 					    e.op == OP_greaterthan ?  lessthan(v1,v0) :
 					    	                     !lessthan(v0,v1);
 				break;
 			}
 			
 			case OP_convert_s:
 				tref = STRING.ref.nonnull();
 				v = eval_convert_s(values.get(e.args[0]));
 				break;
 			
 			case OP_coerce_s:
 			{
 				tref = eval_coerce_s(types.get(e.args[0]));
 				v = eval_coerce_s(values.get(e.args[0]));
 				break;
 			}
 
 			case OP_coerce_o:
 			{
 				Typeref t0 = types.get(e.args[0]);
 				tref = eval_coerce_o(t0);
 				v = eval_coerce_o(values.get(e.args[0]), t0.t);
 				break;
 			}
 
 			case OP_coerce_a:
 			{
 				// ignore upcast.
 				v = values.get(e.args[0]);
 				tref = types.get(e.args[0]);
 				break;
 			}
 			
 			case OP_coerce:
 			{
 				Typeref t0 = types.get(e.args[0]);
 				Object v0 = values.get(e.args[0]);
 				Type t = namedTypes.get(e.ref);
 				if (t == STRING)
 				{
 					tref = eval_coerce_s(t0);
 					v = eval_coerce_s(v0);
 				}
 				else if (t == OBJECT)
 				{
 					tref = eval_coerce_o(t0);
 					v = eval_coerce_o(v0,t0.t);
 				}
 				else if (t == INT)
 				{
 					tref = t.ref;
 					v = eval_convert_i(v0);
 				}
 				else if (t == UINT) 
 				{
 					tref = t.ref;
 					v = eval_convert_u(v0);
 				}
 				else if (t == NUMBER) 
 				{
 					tref = t.ref;
 					v = eval_convert_d(v0);
 				}
 				else if (t == BOOLEAN) 
 				{
 					tref = t.ref;
 					v = eval_convert_b(v0);
 				}
 				else
 				{
 					// pointer style cast
 					if (istype(t0.t, t))
 					{
 						// ignore upcasts
 						tref = t0;
 						v = v0;
 					}
 					else if (t0.t == NULL || t0.t == VOID)
 					{
 						tref = NULL.ref;
 					}
 					else
 					{
 						tref = t.ref;
 					}
 				}
 				break;
 			}
 			
 			case OP_astype:
 				// TODO constant folding
 				tref = namedTypes.get(e.ref).ref;
 				break;
 			
 			case OP_astypelate:
 			{
 				Typeref t1 = types.get(e.args[1]);
 				if (t1.t.itype != null)
 				{
 					if (t1.t.itype.atom || t1.t.itype.numeric)
 						tref = OBJECT.ref;
 					else
 						tref = t1.t.itype.ref;
 				}
 				else
 				{
 					tref = ANY.ref;
 				}
 				break;
 			}
 			
 			case OP_typeof:
 			{
 				Type t0 = type(types,e.args[0]);
 				if (t0 == INT || t0 == UINT || t0 == NUMBER)
 					v = "number";
 				else if (t0 == STRING)
 					v = "string";
 				else if (istype(t0,XML) || istype(t0,XMLLIST))
 					v = "xml";
 				else if (t0 == VOID)
 					v = "undefined";
 				else if (t0 == BOOLEAN)
 					v = "boolean";
 				else if (istype(t0, FUNCTION))
 					v = "function";
 				else if (t0 != OBJECT && istype(t0,OBJECT))
 					v = "object";
 				tref = STRING.ref.nonnull();
 				break;
 			}
 			
 			case OP_add:
 			{
 				Expr a0 = e.args[0];
 				Expr a1 = e.args[1];
 				Typeref t0 = types.get(a0);
 				Typeref t1 = types.get(a1);
 				Object v0 = values.get(a0);
 				Object v1 = values.get(a1);
 				if (t0.t == STRING && !t0.nullable || t1.t == STRING && !t1.nullable)
 				{
 					tref = STRING.ref.nonnull();
 					if (v0 != BOTTOM && v1 != BOTTOM)
 						v = stringValue(v0) + stringValue(v1);
 				}
 				else if (t0.t.numeric && t1.t.numeric)
 				{
 					tref = NUMBER.ref;
 					if (v0 instanceof Number && v1 instanceof Number)
 						v = doubleValue(v0) + doubleValue(v1);
 				}
 				else
 				{
 					// TODO make all primitives extend a type so we can use that type here.
 					tref = OBJECT.ref.nonnull(); // will be a String or a Number
 				}
 				break;
 			}
 			
 			case OP_divide:
 			{
 				tref = NUMBER.ref;
 				Object v0 = values.get(e.args[0]);
 				Object v1 = values.get(e.args[1]);
 				if (v0 instanceof Number && v1 instanceof Number)
 					v = doubleValue(v0) / doubleValue(v1);
 				break;
 			}
 			
 			case OP_subtract:
 			case OP_multiply:
 			case OP_modulo:
 			case OP_negate:
 			case OP_increment:
 			case OP_decrement:
 				tref = NUMBER.ref;
 				break;
 
 			case OP_convert_d:
 				tref = NUMBER.ref;
 				v = eval_convert_d(values.get(e.args[0]));
 				break;
 				
 			case OP_convert_i:
 				tref = INT.ref;
 				v = eval_convert_i(values.get(e.args[0]));
 				break;
 
 			case OP_convert_u:
 				tref = UINT.ref;
 				v = eval_convert_u(values.get(e.args[0]));
 				break;
 	
 			case OP_bitor:
 			{
 				tref = INT.ref;
 				Object v0 = values.get(e.args[0]);
 				Object v1 = values.get(e.args[1]);
 				if (v0 instanceof Number && v1 instanceof Number)
 					v = intValue(v0) | intValue(v1);
 				break;
 			}
 			
 			case OP_bitand:
 			{
 				tref = INT.ref;
 				Object v0 = values.get(e.args[0]);
 				Object v1 = values.get(e.args[1]);
 				if (v0 instanceof Number && v1 instanceof Number)
 				{
 					v = intValue(v0) & intValue(v1);
 				}
 				break;
 			}
 				
 			case OP_bitnot:
 			case OP_add_i:
 			case OP_subtract_i:
 			case OP_multiply_i:
 			case OP_negate_i:
 			case OP_bitxor:
 			case OP_lshift:
 			case OP_rshift:
 			case OP_hasnext2_i:
 			case OP_increment_i:
 			case OP_decrement_i:
 				// TODO constant folding
 				tref = INT.ref;
 				break;
 				
 			case OP_urshift:
 				// TODO constant folding
 				tref = UINT.ref;
 				break;
 				
 			// these ops do not produce any value
 			case OP_setslot:
 			case OP_setproperty:
 			case OP_setsuper:
 			//case OP_setglobalslot: // deprecated
 			case OP_initproperty:
 			case OP_callpropvoid:
 			case OP_constructsuper:
 			case OP_callsupervoid:
 			case OP_returnvoid:
 			case OP_returnvalue:
 			case OP_throw:
 			case OP_popscope:
 			case OP_debug:
 			case OP_debugline:
 			case OP_debugfile:
 			case OP_bkpt:
 			case OP_bkptline:
 				return;
 			}
 		}
 		
 		assert(tref != null && tref.t != null);
 		
 		// singleton types have a specific value.
 		if (tref.t == VOID)
 			v = UNDEFINED;
 		else if (tref.t == NULL)
 			v = NULL;
 		
 		if (v != null && !v.equals(values.get(e)))
 		{
 			values.put(e, v);
 			ssaWork.addAll(uses.get(e));
 		}
 		
 		if (!tref.equals(types.get(e)))
 		{
 			types.put(e, tref);
 			ssaWork.addAll(uses.get(e));
 		}
 	}
 	
 	Typeref verify_eval(Method m, Expr e,
 			Map<Expr,Typeref> types, Map<Block,Block> idom)
 	{
 		Typeref tref = null;
 				
 		if (e.op == OP_phi)
 		{
 			boolean loop = false;
 			for (int i=e.args.length-1; i >= 0; i--)
 			{
 				Expr a = e.args[i];
 				loop |= isLoop(e.pred[i],idom);
 				// compute the phi() meet operation.  ignore any inputs we
 				// haven't evaluated yet.  If all inputs have the same value,
 				// phi() has that value.  Otherwise return BOTTOM.
 				
 				// same idea for types, but if they aren't all the same
 				// then compute the most derived base class (mdb) of the types.
 				Typeref aref = types.get(a);
 				if (aref == null)
 					continue; // ignore unprocessed inputs
 				if (tref == null)
 					tref = aref;
 				else if (!tref.equals(aref))
 					tref = mdb(tref,aref);
 			}
 			// make nullable types nullable at top of loop so verifier
 			// won't ever need to iterate.
 			if (loop)
 				tref = tref.t.ref;
 		}
 		else
 		{
 			tref = ANY.ref;
 			
 			// if any arg is TOP result is TOP (unchanged)
 			for (Expr a : e.args) if (!types.containsKey(a))	return tref;
 			for (Expr a : e.scopes) if (!types.containsKey(a))	return tref;
 			for (Expr a : e.locals) if (!types.containsKey(a))	return tref;
 			
 			switch (e.op)
 			{
 			default:
 				assert(false);
 			
 			case OP_hasnext2_o:
 			case OP_nextname:
 			case OP_nextvalue:
 			case OP_call:
 			case OP_getsuper:
 			case OP_getdescendants:
 				tref = ANY.ref;
 				break;
 				
 			case OP_convert_o:
 				// bug in verifier -- doesn't set state nonnull here.
 				tref = types.get(e.args[0]);
 				//tref = types.get(e.args[0]).nonnull();
 				break;
 			
 			case OP_esc_xattr:
 			case OP_esc_xelem:
 				tref = STRING.ref.nonnull();
 				break;
 				
 			case OP_newcatch:
 				tref = m.handlers[e.imm[0]].activation;
 				break;
 				
 			case OP_newobject:
 				tref = OBJECT.ref.nonnull();
 				break;
 			
 			case OP_newarray:
 				tref = ARRAY.ref.nonnull();
 				break;
 				
 			case OP_newactivation:
 				tref = m.activation;
 				break;
 				
 			case OP_getglobalscope:
 				if (m.cx.scopes.length > 0)
 				{
 					tref = m.cx.scopes[0];
 				}
 				else
 				{
 					// same as getscopeobject<0>
 					tref = types.get(e.scopes[0].args[0]);
 				}
 				break;
 				
 			case OP_getscopeobject:
 				tref = types.get(e.scopes[0].args[0]);
 				break;
 				
 			case OP_newclass:
 				tref = e.c.ref.nonnull();
 				break;
 				
 			case OP_newfunction:
 				tref = FUNCTION.ref.nonnull();
 				break;
 				
 			case OP_finddef:
 				if (globals.contains(e.ref))
 					tref = globals.get(e.ref);
 				break;
 				
 			case OP_findpropstrict:
 			case OP_findproperty:
 			{
 				int i = findInner(e.ref, e.scopes, types);
 				if (i >= 0)
 				{
 					tref = types.get(e.scopes[i]);
 				}
 				else if ((i = findOuter(e.ref, m.cx.scopes)) >= 0)
 				{
 					tref = m.cx.scopes[i];
 				}
 				else if (globals.contains(e.ref))
 				{
 					tref = globals.get(e.ref);
 				}
 				else
 				{
 					// not found.  use global.
 					if (m.cx.scopes.length > 0)
 					{
 						tref = m.cx.scopes[0];
 					}
 					else
 					{
 						tref = types.get(e.scopes[0]);
 					}
 				}
 				break;
 			}
 			
 			case OP_getlex:
 			{
 				// findproperty + getproperty
 				int i = findInner(e.ref, e.scopes, types);
 				Typeref stref = i >= 0 ? types.get(e.scopes[i]) : 
 					(i=findOuter(e.ref, m.cx.scopes)) >= 0 ? m.cx.scopes[i] :
 						globals.contains(e.ref) ? globals.get(e.ref) :
 							m.cx.scopes.length > 0 ? m.cx.scopes[0] :
 								types.get(e.scopes[0]);
 	
 				Binding b = stref.t.findGet(e.ref);
 				tref = verify_eval_getproperty(tref, b);
 				break;
 			}
 			
 			case OP_construct:
 			{
 				tref = OBJECT.ref.nonnull();
 				break;
 			}
 			
 			case OP_constructprop:
 			{
 				Type ot = type(types,e.args[0]); // type of base object
 				Binding b = ot.findGet(e.ref);
 				if (b != null && b.type != null && b.type.t.itype != null)
 				{
 					tref = b.type.t.itype.ref.nonnull();
 					break;
 				}
 				break;
 			}
 			
 			case OP_callproperty:
 			case OP_callproplex:
 			{
 				Type ot = type(types, e.args[0]);
 				Binding b = ot.findGet(e.ref);
 				if (isMethod(b))
 				{
 					tref = b.method.returns;
 				}
 				else if (isSlot(b) && b.type != null && b.type.t.itype != null)
 				{
 					// calling a class as a function, return type is cast to instance type
 					// issue what about special cases? RegExp? Date?
 					tref = b.type.t.itype.ref;
 				}
 				break;
 			}
 			
 			case OP_callsuper:
 			{
 				Type ot = m.cx.base;
 				Binding b = ot.findGet(e.ref);
 				if (isMethod(b))
 				{
 					tref = b.method.returns;
 				}
 				break;
 			}
 			
 			case OP_applytype:
 				tref = types.get(e.args[0]).nonnull();
 				break;
 			
 			case OP_callstatic:
 				tref = e.m.returns;
 				break;
 			
 			case OP_arg:
 				if (e.imm[0] < m.params.length)
 					tref = m.params[e.imm[0]];
 				else if (m.needsArguments()||m.needsRest() && e.imm[0] == m.params.length)
 					tref = ARRAY.ref.nonnull();
 				else
 					tref = VOID.ref;
 				break;
 				
 			case OP_xarg:
 				tref = m.handlers[e.imm[0]].type;
 				break;
 				
 			case OP_getslot:
 			{
 				Type t0 = type(types, e.args[0]);
 				Binding b = t0.findSlot(e.imm[0]);
 				if (b != null)
 					tref = b.type;
 				break;
 			}
 			
 			case OP_getproperty:
 			{
 				Type t0 = type(types, e.args[0]);
 				Binding b = t0.findGet(e.ref);
 				tref = verify_eval_getproperty(tref, b);
 				break;
 			}
 			
 			case OP_kill:
 				tref = ANY.ref;
 				break;
 				
 			case OP_pushundefined:
 				tref = VOID.ref;
 				break;
 				
 			case OP_pushnull:
 				tref = NULL.ref;
 				break;
 
 			case OP_pushnamespace:
 				tref = NAMESPACE.ref.nonnull();
 				break;
 							
 			case OP_pushscope:
 			case OP_pushwith:
 				// treat this as a copy.
 				tref = types.get(e.args[0]).nonnull();
 				break;
 				
 			case OP_pushtrue:
 			case OP_pushfalse:
 			case OP_convert_b:
 			case OP_not:
 			case OP_deleteproperty:
 			case OP_deldescendants:
 			case OP_hasnext:
 			case OP_hasnext2:
 			case OP_equals:
 			case OP_strictequals:
 			case OP_in:
 			case OP_istype:
 			case OP_istypelate:
 			case OP_instanceof:
 			case OP_lessthan:
 			case OP_lessequals:
 			case OP_greaterthan:
 			case OP_greaterequals:
 				tref = BOOLEAN.ref;
 				break;
 			
 			case OP_convert_s:
 			case OP_pushstring:
 				tref = STRING.ref.nonnull();
 				break;
 			
 			case OP_coerce_s:
 			{
 				Typeref t0 = types.get(e.args[0]);
 				tref = new Typeref(STRING, t0.nullable);
 				break;
 			}
 
 			case OP_coerce_o:
 			{
 				Typeref t0 = types.get(e.args[0]);
 				tref = new Typeref(OBJECT, t0.nullable);
 				break;
 			}
 
 			case OP_coerce_a:
 			{
 				Typeref t0 = types.get(e.args[0]);
 				tref = new Typeref(ANY, t0.nullable);
 				break;
 			}
 			
 			case OP_coerce:
 				tref = namedTypes.get(e.ref).ref;
 				break;
 			
 			case OP_astype:
 			{
 				Typeref t0 = types.get(e.args[0]);
 				Type t = namedTypes.get(e.ref);
 				if (!istype(t0.t, t) || isAtom(t0.t) != isAtom(t))
 				{
 					// TODO figure out what verifier is really doing here.
 					tref = t.ref;
 				}
 				else
 				{
 					tref = t0;
 				}
 				break;
 			}
 			
 			case OP_astypelate:
 			{
 				Typeref t1 = types.get(e.args[1]);
 				if (t1.t.itype != null)
 				{
 					if (t1.t.itype.atom || t1.t.itype.numeric)
 						tref = OBJECT.ref;
 					else
 						tref = t1.t.itype.ref;
 				}
 				else
 				{
 					tref = ANY.ref;
 				}
 				break;
 			}
 				
 			case OP_typeof:
 				tref = STRING.ref.nonnull();
 				break;
 			
 			case OP_add:
 			{
 				Expr a0 = e.args[0];
 				Expr a1 = e.args[1];
 				Typeref t0 = types.get(a0);
 				Typeref t1 = types.get(a1);
 				if (t0.t == STRING && !t0.nullable || t1.t == STRING && !t1.nullable)
 				{
 					tref = STRING.ref.nonnull();
 				}
 				else if (t0.t.numeric && t1.t.numeric)
 				{
 					tref = NUMBER.ref;
 				}
 				else
 				{
 					// TODO make all primitives extend a type so we can use that type here.
 					tref = OBJECT.ref.nonnull(); // will be a String or a Number
 				}
 				break;
 			}
 			
 			case OP_divide:
 			case OP_subtract:
 			case OP_multiply:
 			case OP_modulo:
 			case OP_negate:
 			case OP_increment:
 			case OP_decrement:
 			case OP_convert_d:
 			case OP_pushnan:
 			case OP_pushdouble:
 				tref = NUMBER.ref;
 				break;
 
 			case OP_convert_i:
 			case OP_bitor:
 			case OP_bitand:
 			case OP_add_i:
 			case OP_subtract_i:
 			case OP_multiply_i:
 			case OP_negate_i:
 			case OP_bitxor:
 			case OP_lshift:
 			case OP_rshift:
 			case OP_hasnext2_i:
 			case OP_increment_i:
 			case OP_decrement_i:
 			case OP_pushbyte:
 			case OP_pushshort:
 			case OP_pushint:
 			case OP_bitnot:
 				tref = INT.ref;
 				break;
 				
 			case OP_pushuint:
 			case OP_convert_u:
 			case OP_urshift:
 				tref = UINT.ref;
 				break;
 			}
 		}
 		
 		assert(tref != null && tref.t != null);
 		return tref;
 	}
 
 	private Typeref verify_eval_getproperty(Typeref tref, Binding b)
 	{
 		if (isSlot(b))
 		{
 			tref = b.type;
 		}
 		else if (isMethod(b))
 		{
 			//tref = FUNCTION.ref.nonnull(); // TODO use MethodClosure
 			// avmplus uses ANY here. see Verifier::readBinding().
 			tref = ANY.ref;
 		}
 		else if (isGetter(b))
 		{
 			tref = b.method.returns;
 		}
 		return tref;
 	}
 
 	/**
 	 * find the set of def->use edges.  These are the opposite of the
 	 * use->def edges encoded in Expr.args[] and scopes[]. 
 	 * @param code
 	 * @return
 	 */
 	EdgeMap<Expr> findUses(Deque<Block> code)
 	{
 		EdgeMap<Expr> uses = new EdgeMap<Expr>();
 		for (Block b : code)
 			for (Expr e : b)
 			{
 				for (Expr a : e.args) uses.get(a).add(e);
 				for (Expr a : e.locals) uses.get(a).add(e);
 				for (Expr a : e.scopes) uses.get(a).add(e);
 			}
 		return uses;
 	}
 	
 	/**
 	 * most derived base
 	 * @param a
 	 * @param b
 	 * @return
 	 */
 	Typeref mdb(Typeref a, Typeref b)
 	{
 		// TODO support interfaces
 		assert(a != b && a != null && b != null);
 		
 		// null is compatible with pointer types
 		if (a.t == NULL && isPointer(b.t)) return b;
 		if (b.t == NULL && isPointer(a.t)) return a;
 		
 		Set<Type> bases = new HashSet<Type>();
 		for (Type t = a.t; t != null; t = t.base)
 			bases.add(t);
 		for (Type t = b.t; t != null; t = t.base)
 			if (bases.contains(t))
 				return new Typeref(t, a.nullable | b.nullable);
 		return new Typeref(ANY, a.nullable | b.nullable);
 	}
 	
 	/**
 	 * find inner scope.  returns index of object or -1 if not found.
 	 * @param ref
 	 * @param scopes
 	 * @param types
 	 * @return
 	 */
 	int findInner(Name ref, Expr[] scopes, Map<Expr,Typeref> types)
 	{
 		for (int i=scopes.length-1; i >= 0; i--)
 		{
 			Type st = type(types,scopes[i]);
 			//verboseStatus("finding "+ref);
 			Binding b = st.find(ref);
 			if (b != null)
 				return i;
 		}
 		return -1;
 	}
 	
 	/**
 	 * find outer scope.  returns index, or -1 if not found, in which
 	 * case caller can look in globals.
 	 * 
 	 * @param ref
 	 * @param scopes
 	 * @return
 	 */
 	int findOuter(Name ref, Typeref[] scopes)
 	{
 		for (int i=scopes.length-1; i >= 1; i--)
 		{
 			Type st = scopes[i].t;
 			Binding b = st.find(ref);
 			if (b != null)
 				return i;
 		}
 		Typeref st = globals.get(ref);
 		if (st != null)
 			return -1; // how to identify which global?
 		if (scopes.length > 0 && scopes[0].t.find(ref) != null)
 			return 0;
 		return -1; // can't find it (caller can search globals, will return null).
 	}
 	
 	boolean istype(Type t, Type c)
 	{
 		// TODO add interface support
 		for (; t != null; t = t.base)
 			if (t == c) 
 				return true;
 		return false;
 	}
 	
 	static class SetMap<K,V extends Indexable> extends TreeMap<K, Set<V>>
 	{
 		public Set<V> get(K e)
 		{
 			Set<V> s = super.get(e);
 			if (s == null)
 				put(e,s = new TreeSet<V>());
 			return s;
 		}
 		static final long serialVersionUID=0;
 	}
 	
 	static class EdgeMap<E extends Indexable> extends SetMap<E,E>
 	{
 		public Set<E> get(E e)
 		{
 			return super.get(e);
 		}
 		static final long serialVersionUID=0;
 	}
 	
 	static class WorkSet<E extends Indexable> extends TreeSet<E>
 	{
 		static final long serialVersionUID=0;
 	}
 	
 	boolean isCritical(Edge e, SetMap<Block,Edge>pred)
 	{
 		return e.from.succ().length > 1 && pred.get(e.to).size() > 1;
 	}
 	
 	void split(Edge e, Method m, SetMap<Block,Edge>pred)
 	{
 		assert(e.handler == null); // can't split exception edges
 		verboseStatus("SPLIT "+e);
 		Expr j = new Expr(m, OP_jump);
 		Block d = new Block(m);
 		Block to = e.to;
 		Edge e2 = new Edge(m,d,0,to);
 		j.succ = new Edge[] { e2 };
 		d.add(j);
 		e.to = d;
 		pred.get(d).add(e);
 		pred.get(to).remove(e);
 		pred.get(to).add(e2);
 		replacePred(to,e,e2);
 	}
 	
 	void replacePred(Block b, Edge before, Edge after)
 	{
 		for (Expr e: b)
 		{
 			if (e.op == OP_phi)
 			{
 				for (int i=0, n=e.pred.length; i < n; i++)
 					if (e.pred[i] == before)
 						e.pred[i] = after;
 			}
 		}
 	}
 	
 	Expr append(Edge edge, Expr e)
 	{
 		Deque<Expr> exprs = edge.from.exprs;
 		Expr last = exprs.removeLast();
 		exprs.add(e);
 		exprs.add(last);
 		return e;
 	}
 	Expr prepend(Edge edge, Expr e)
 	{
 		Deque<Expr> exprs = edge.from.exprs;
 		exprs.addFirst(e);
 		return e;
 	}
 	
 	Expr setlocal(Method m, int i, Expr a)
 	{
 		return new Expr(m, OP_setlocal, i, new Expr[] { a }, 1, 1);
 	}
 	
 	Expr getlocal(Method m, int i)
 	{
 		return new Expr(m, OP_getlocal, i);
 	}
 	
 	Expr dup(Method m, Expr e)
 	{
 		Expr dup = new Expr(m, OP_dup);
 		dup.locals = new Expr[] { e };
 		return dup;
 	}
 	
 	/**
 	 * the last phase before emitting code.
 	 * - stack scheduling
 	 * - scope scheduling
 	 * - assign local variables
 	 * - replace phi's with copies if necessary
 	 * - recompute frame size
 	 * @param code
 	 */
 	void remove_phi(Method m)
 	{
 		Deque<Block> code = dfs(m.entry.to);
 		SetMap<Block,Edge> pred = preds(code);
 
 		Map<Integer,Integer> locals = new TreeMap<Integer,Integer>();
 		Map<Block,Deque<Expr>> exprs = new TreeMap<Block,Deque<Expr>>();
 		ConflictGraph conflicts = new ConflictGraph();
 		
 		verboseStatus("BEFORE SCHED");
 		print(code);
 		
 		restused:
 		if (m.needsArguments() || m.needsRest())
 		{
 			// if we have a rest param or arguments and we don't use it,
 			// clear those flags and set the ignore-extra-args flag. 
 			int rest = m.params.length;
 			for (Expr e: m.entry.to)
 				if (e.op == OP_arg && e.imm[0] == rest)
 					break restused;
 			m.flags &= ~(METHOD_Arguments|METHOD_Needrest);
 			m.flags |= METHOD_IgnoreRest;
 			verboseStatus("IGNORE_REST for "+m.name);
 		}
 
 		sched_greedy(m, code, locals, pred, exprs, conflicts);
 		
 		alloc_locals(code, locals, conflicts);
 
 		int max_local = m.params.length-1;
 		int max_stack = 0;
 		int max_scope = 0;
 		Map<Block,Integer>stkin = new TreeMap<Block,Integer>();
 		Map<Block,Integer>scpin = new TreeMap<Block,Integer>();
 		stkin.put(m.entry.to, 0);
 		scpin.put(m.entry.to, 0);
 		
 		
 		// insert phi copies on edges where needed
 		Set<Edge> splits = new TreeSet<Edge>();
 		for (Block b: code)
 		{
 			// identify the edges that need copies, split them, add copies.
 			// only split an edge once during this whole pass.
 			for (Expr e: b)
 			{
 				if (e.op != OP_phi)
 					break;
 				if (!locals.containsKey(e.id))
 					continue;
 				int lhs = locals.get(e.id);
 				for (int i=e.args.length-1; i>=0; i--)
 				{
 					int rhs = locals.get(e.args[i].id);
 					if (lhs != rhs)
 					{
 						Edge p = e.pred[i];
 						if (!splits.contains(p))
 						{
 							split(p,m,pred);
 							splits.add(p = e.pred[i]);
 						}
 						Expr get = getlocal(m, rhs);
 						prepend(p, get);
 						append(p, setlocal(m,lhs,get));
 					}
 				}
 			}
 			
 			// now swap in the scheduled code and renumber the get/setlocals we have in there.
 			b.exprs = exprs.get(b);
 			int stkdepth = stkin.get(b);
 			int scpdepth = scpin.get(b);
 			for (Expr e: b)
 			{
 				assert(!e.isSynthetic());
 				
 				// compute max_stack
 				assert(stkdepth >= e.args.length);
 				stkdepth -= e.args.length;
 				if (e.onStack())
 					stkdepth++;
 				if (stkdepth > max_stack)
 					max_stack = stkdepth;
 				
 				// compute max_scope
 				assert(scpdepth >= e.scopes.length);
 				if (e.op == OP_popscope)
 					scpdepth--;
 				else if (e.onScope())
 					scpdepth++;
 				if (scpdepth > max_scope)
 					max_scope = scpdepth;
 				
 				// assign locals & update local_count
 				int loc = max_local;
 				if (e.op == OP_getlocal || e.op == OP_setlocal)
 				{
 					loc = e.imm[0] = locals.get(e.imm[0]);
 				}
 				else if (e.op == OP_hasnext2)
 				{
 					int loc0 = locals.get(e.locals[0].id);
 					int loc1 = locals.get(e.locals[1].id);
 					e.imm = new int[] { loc0, loc1 };
 					loc = loc0 > loc1 ? loc0 : loc1;
 				}
 				if (loc > max_local)
 					max_local = loc;
 			}
 			for (Edge s: b.succ())
 				update_depth(s.to, stkdepth, stkin, scpdepth, scpin);
 			for (Edge s: b.xsucc)
 				update_depth(s.to, 1, stkin, 0, scpin);
 		}
 		
 		if ( legacy_verifier )
 			killCruftyLocals(m); 
 		
 		m.local_count = max_local+1;
 		m.max_stack = max_stack;
 		m.max_scope = max_scope;
 		
 		verboseStatus("AFTER SCHED "+m.name+" local_count="+m.local_count+" max_stack="+max_stack+" max_scope="+max_scope);
 		
 		// some of the edges we split didn't need to be.
 		cfgopt(m);
 	}
 	
 	/**
 	 *  Work around legacy verifier restrictions:
 	 *  Kill predecessor blocks' locals if they're live out
 	 *  on more than one path and not live in to a block.
 	 *  @param m - the Method under analysis.
 	 */
 	void killCruftyLocals(Method m)
 	{
 		Deque<Block> code = dfs(m.entry.to);
 		SetMap<Block, Edge>pred = preds(code);
 		
 		Map<Block,LocalVarState> local_state = getLocalVarState(code, pred);
 		
 		Map<Block, BitSet> killed_vars = new TreeMap<Block, BitSet>();
 		
 		for ( Block b: code)
 		{
 			//  Work around legacy verifier restrictions:
 			//  Kill predecessor blocks' locals if they're
 			//  active on more than one path and not phi inputs.
 			killed_vars.put(b, new BitSet() );
 			
 			Set<Edge> current_preds = pred.get(b);
 			
 			if ( current_preds.size() > 1)
 			{
 				LocalVarState current_state = local_state.get(b);
 				// BitSet current_livein = current_state.getLivein();
 				
 				//  Find variables that come in on multiple paths.
 				Map<Integer, Set<Edge>> livein_edges = new TreeMap<Integer,Set<Edge>>();
 				
 				for ( Edge p: current_preds )
 				{
 					BitSet pred_liveout = local_state.get(p.from).getLiveout();
 					
 					for ( int varnum = 0; varnum < pred_liveout.length(); varnum++)
 					{
 						if ( pred_liveout.get(varnum) )
 						{
 							if ( livein_edges.get(varnum) == null )
 							{
 								livein_edges.put(varnum, new TreeSet<Edge>());
 							}
 							
 							livein_edges.get(varnum).add(p);
 						}
 					}
 				}
 				
 				//  Find locals coming from more than one predecessor
 				//  whose types don't match; these need to be killed
 				//  if they're not in the current block's livein set
 				//  (in which case they will have been a phi input).
 				for ( Integer suspect_local: livein_edges.keySet() )
 				{
 					if ( livein_edges.get(suspect_local).size() > 1 )
 					{
 						for ( Edge p: livein_edges.get(suspect_local))
 						{
 							if ( ! (current_state.isPhiInput(p, suspect_local) || current_state.getLivein().get(suspect_local) || killed_vars.get(b).get(suspect_local)) )
 							{	
 								p.to.exprs.addFirst(new Expr(m, OP_kill, suspect_local.intValue()));
 								killed_vars.get(b).set(suspect_local);
 							}
 						}
 					}
 				}
 				
 			}
 		}
 	}
 
 	void alloc_locals(Deque<Block> code, Map<Integer, Integer> locals, ConflictGraph conflicts)
 	{
 		for (Block b: code)
 			for (Expr e: b)
 				if (locals.containsKey(e.id))
 					alloc1(e,conflicts,locals);
 		for (Block b: code)
 			for (Expr e: b)
 				if (locals.containsKey(e.id))
 					alloc2(e,conflicts,locals);
 
 		verboseStatus("CONFLICTS " + conflicts);
 		verboseStatus("LOCALS "+locals);
 	}
 	
 	void update_depth(Block b, int stkdepth, Map<Block,Integer> stkin, int scpdepth, Map<Block,Integer> scpin)
 	{
 		if (stkin.containsKey(b))
 			assert(stkin.get(b) == stkdepth);
 		else
 			stkin.put(b,stkdepth);
 
 		if (scpin.containsKey(b))
 			{}//			assert(scpin.get(b) == scpdepth);
 		else
 			scpin.put(b,scpdepth);
 	}
 	
 	void allocate(int id, int loc, Map<Integer,Integer>locals, ConflictGraph conflicts)
 	{
 		assert(locals.get(id) == -1 && loc != -1);
 		locals.put(id, loc);
 		
 		// sanity check
 		for (int j: conflicts.get(id))
 			assert(locals.get(j) != loc);
 	}
 	
 	void alloc1(Expr e, ConflictGraph conflicts, Map<Integer,Integer> locals)
 	{
 		if (locals.get(e.id) != -1)
 			return;
 
 		BitSet used = new BitSet();
 		for (int i: conflicts.get(e.id))
 			if (locals.get(i) != -1)
 				used.set(locals.get(i));
 		
 		int loc = -1;
 		//  If the expression's locked to a specific local (e.g., it's an arg), it must use that local.
 		if (e.locals.length == 1 && e.inLocal() && locals.containsKey(e.locals[0].id) && (loc=locals.get(e.locals[0].id)) != -1)
 		{
 			assert(!used.get(loc));
 			allocate(e.id, loc, locals, conflicts);
 		}
 		
 		if (e.op != OP_phi)
 			return;
 		
 		//  If any phi input is already allocated, and the local's available, use that slot. 
 		for (Expr a: e.args)
 		{
 			if (locals.containsKey(a.id) && (loc=locals.get(a.id)) != -1 && !used.get(loc))
 			{
 				allocate(e.id, loc, locals, conflicts);
 				break;
 			}
 		}
 
 		if (loc == -1)
 		{
 			//  No previous allocation found; allocate lowest-numbered free local.
 			loc = 0;
 			while (used.get(loc))
 				loc++;
 			allocate(e.id, loc, locals, conflicts);
 		}
 		
 		// try to assign all inputs to the same local to avoid copies
 		nextarg:
 		for (Expr a: e.args)
 		{
 			if (locals.get(a.id) == -1)
 			{
 				for (int j: conflicts.get(a.id))
 					if (locals.get(j) == loc)
 						continue nextarg;
 				allocate(a.id, loc, locals, conflicts);
 			}
 		}
 	}
 
 	void alloc2(Expr e, ConflictGraph conflicts, Map<Integer,Integer> locals)
 	{
 		if (locals.get(e.id) != -1)
 			return;
 
 		BitSet used = new BitSet();
 		for (int i: conflicts.get(e.id))
 			if (locals.get(i) != -1)
 				used.set(locals.get(i));
 		
 		int loc;
 		if (e.locals.length == 1 && e.inLocal() && locals.containsKey(e.locals[0].id) && (loc=locals.get(e.locals[0].id)) != -1)
 		{
 			// must use same local as arg
 			assert(!used.get(loc));
 			allocate(e.id, loc, locals, conflicts);
 			//return;
 		}
 		
 		if (e.args.length != 0 && locals.containsKey(e.args[0].id) && (loc=locals.get(e.args[0].id)) != -1 &&
 				!used.get(loc))
 		{
 			// try to use same local as arg
 			allocate(e.id, loc, locals, conflicts);
 			return;
 		}
 
 		// otherwise assign the lowest numbered free local
 		loc = 0;
 		while (used.get(loc))
 			loc++;
 		allocate(e.id, loc, locals, conflicts);
 	}
 	
 	Map<Block,LocalVarState> getLocalVarState(Deque<Block> code, SetMap<Block,Edge> pred)
 	{
 		
 		Map<Block,LocalVarState> result = new TreeMap<Block,LocalVarState>();
 				
 		for ( Block b: code)
 		{
 			buildLocalMap(b, result, pred);
 		}
 		
 		//  Compute liveout sets.
 		computeLiveout(code, result);
 		
 		return result;
 	}
 	
 	LocalVarState buildLocalMap(Block b, Map<Block,LocalVarState> local_map, SetMap<Block,Edge> preds)
 	{
 	
 		LocalVarState result = local_map.get(b);
 		
 		if ( null == result )
 		{
 			result = new LocalVarState(b);
 			local_map.put(b, result);
 		}
 		
 		return result;
 	}
 	
 	/**
 	 *  Compute variable live-out sets for a control-flow graph.
 	 *  Follows Cooper & Torczon's [section 9.2.2] Round-robin, iterative solver
 	 * @param code - The CFG.  Presumed to be in DFS order (not a precondition).
 	 * @param live_map - the result map of blocks to sets of live-out variables.
 	 * @pre live_map must already have per-block data 
 	 *   for upwardly exposed vars, defines, and kills.
 	 */
 	void computeLiveout(Deque<Block> code, Map<Block,LocalVarState> live_map)
 	{
 		//  
 		boolean changed = true;
 		
 		while ( changed )
 		{
 			changed = false;
 			
 			for ( Block b: code)
 			{
 				LocalVarState current_state = live_map.get(b);
 				
 				BitSet next_liveout = new BitSet();
 				
 				for ( Edge p: b.succ() )
 				{
 					next_liveout.or(live_map.get(p.to).getLivein());
 				}
 				
 				changed |= current_state.mergeLiveout(next_liveout);
 			}
 		}
 	}
 	
 	static interface Deque<E> extends List<E>
 	{
 		E removeFirst();
 		E peekFirst();
 		E removeLast();
 		E peekLast();
 		void addFirst(E e);
 	}
 	
 	static class ArrayDeque<E> extends ArrayList<E> implements Deque<E>
 	{
 		ArrayDeque()
 		{
 		}
 		ArrayDeque(Collection<E> c)
 		{
 			addAll(c);
 		}
 		public void addFirst(E e)
 		{
 			add(0, e);
 		}
 		public E removeFirst()
 		{
 			return remove(0);
 		}
 		public E peekFirst()
 		{
 			return isEmpty() ? null : get(0);
 		}
 		public E removeLast()
 		{
 			return remove(size()-1);
 		}
 		public E peekLast()
 		{
 			return isEmpty() ? null : get(size()-1);
 		}
 		public static final long serialVersionUID = 0;
 	}
 	
 	static class LinkedDeque<E> extends LinkedList<E> implements Deque<E>
 	{
 		public E peekFirst()
 		{
 			return isEmpty() ? null : getFirst();
 		}
 		public E peekLast()
 		{
 			return isEmpty() ? null : getLast();
 		}
 		public static final long serialVersionUID = 0;
 	}
 	
 	static class ConflictGraph
 	{
 		Map<Integer,Set<Integer>> conflicts = new TreeMap<Integer,Set<Integer>>();
 		
 		void add(Expr e1, Expr e2)
 		{
 			get(e1.id).add(e2.id);
 			get(e2.id).add(e1.id);
 		}
 		
 		boolean contains(Expr a, Expr b)
 		{
 			return conflicts.containsKey(a.id) && conflicts.get(a.id).contains(b.id);
 		}
 		
 		public String toString()
 		{
 			return String.valueOf(conflicts);
 		}
 		
 		Set<Integer> get(int i)
 		{
 			Set<Integer> s = conflicts.get(i);
 			if (s == null)
 				conflicts.put(i, s = new TreeSet<Integer>());
 			return s;
 		}
 	}
 	
 	boolean hasStackEffect(Expr e)
 	{
 		if (e == null || e.op == OP_arg)
 			return true;
 		return e.onStack() || e.args.length > 0;
 	}
 	
 	/**
 	 * Schedule the expressions in the method to maximize use of the operand
 	 * stack, by processing the flow graph and expressions backwards.  If we can't
 	 * issue an instruction to fill a stack slot then we issue a getlocal instead,
 	 * mark the expression live.
 	 * 
 	 * When an expression is marked live, add a conflict edge with each other live
 	 * expression at that point. [Chotin paper?]
 	 * 
 	 * Phi expressions are handled specially - we make each argument live
 	 * at the end of the corresponding predecessor block.  Since the stack is empty
 	 * at the end of each BB, all phi nodes are allocated to a variable.  However,
 	 * later, we attempt to allocate locals so phi nodes don't require copies.
 	 * 
 	 * @param code
 	 * @param locals
 	 * @param pred
 	 * @return
 	 */
 
 	ConflictGraph sched_greedy(Method m, 
 			Deque<Block> code, 
 			Map<Integer,Integer> locals, 
 			SetMap<Block,Edge>pred,
 			Map<Block,Deque<Expr>> exprs,
 			ConflictGraph cg)
 	{
 		SetMap<Block,Expr> liveout = new SetMap<Block,Expr>();
 		Map<Block,Deque<Expr>> stkout = new TreeMap<Block,Deque<Expr>>();
 		Map<Block,Deque<Expr>> scpout = new TreeMap<Block,Deque<Expr>>();
 		HashMap<Block,Deque<Object>> listings = new HashMap<Block,Deque<Object>>();
 		
 		// low postorder numbers are first, 
 		PriorityQueue<Block> work = new PriorityQueue<Block>(code.size(),new Comparator<Block>() {
 			public int compare(Block b1,Block b2) {
 				return b1.postorder-b2.postorder;
 			}
 		});
 		
 		work.addAll(code);
 
 		while (!work.isEmpty())
 		{
 			Block b = work.remove();
 			while (work.peek() == b) 
 				work.remove(); // remove dups
 
 			Set<Expr> live = new TreeSet<Expr>();
 			Deque<Expr> in = new ArrayDeque<Expr>(b.exprs);
 			Deque<Expr> stk = new ArrayDeque<Expr>();
 			Deque<Expr> scp = new ArrayDeque<Expr>();
 			Deque<Object> verbose = new LinkedDeque<Object>();
 			Deque<Expr> out = new LinkedDeque<Expr>();
 
 			Set<Expr> out_of_order = new TreeSet<Expr>();
 			
 			verboseStatus("Block " + b.toString());
 
 			live.addAll(liveout.get(b));
 			for (Expr l: live)
 				locals.put(l.id, l.op == OP_arg ? l.imm[0] : -1);
 			exprs.put(b,out);
 			listings.put(b,verbose);
 
 			Set<Expr>phis = new TreeSet<Expr>();
 			while (!in.isEmpty() || !stk.isEmpty())
 			{
 				while (!stk.isEmpty() && hasStackEffect(in.peekLast()))
 				{
 					showstate(live, stk, scp, verbose);
 					
 					Expr e = remove_dup(stk, m, out, verbose);
 					verboseStatus("Popped " + formatExpr(e) + " from stack");
 					
 					if (e.inLocal() || e.op == OP_phi ||
 						e != in.peekLast() || stk.contains(e))
 					{
 						/*
 						if ( e != in.peekLast() && ! e.hasEffect() )
 						{
 							verboseStatus(".. out-of-order, but issue " + e);
 							issue_expr(e, m, out, verbose, stk, scp, live, locals);
 							out_of_order.add(e);
 						}
 						else
 						*/
 						{
 							if ( verbose_mode )
 							{
 								verboseStatus(" loading " + e.toString() + " because ");
 								if ( e.inLocal() ) verboseStatus("e.inLocal()");
 								if ( e.op == OP_phi ) verboseStatus("e.op == OP_phi");
 								if ( e != in.peekLast() ) verboseStatus(e.toString() + " != last Expr: " + formatExpr(in.peekLast()) );
 								if ( stk.contains(e) ) verboseStatus("stk.contains(" + e.toString() + ")");
 							}
 							issue_load(e, m, out, verbose, live, locals);
 						}
 					}
 					else if (live.contains(e))
 					{
 						// stack top ready to be issued but it's live. issue store.
 						// push e back twice: once for store, once to consume.  then
 						// go around loop once more to handle the dup + issue.
 						verboseStatus(" define() and issue_store() live Expr " + e.toString());
 						define(e,live,cg);
 						issue_store(e, m, out, verbose, stk);
 						verboseStatus("  pushing" + e.toString() + "back on stack");
 						stk.add(e);
 					}
 					else if (e.op == OP_xarg)
 					{
 						while (!stk.isEmpty())
 							loadTOS(m, stk, scp, out, verbose, live, locals);
 						verbose.addFirst(in.removeLast());
 					}
 					else
 					{
 						// e is ready to issue
 						verboseStatus(" issuing " + e);
 						issue_expr(in.removeLast(), m, out, verbose, stk, scp, live, locals);
 					}
 					continue;
 				}
 
 				if (!in.isEmpty())
 				{
 					showstate(live, stk, scp, verbose);
 					Expr e = in.removeLast();
 					verboseStatus("Processing next insn " + formatExpr(e));
 					if ( out_of_order.contains(e) )
 					{
 						verboseStatus(".. already issued " + e );
 						continue;
 					}
 					else if (e.op == OP_phi)
 					{
 						issue_phi(e, verbose, phis, live, cg);
 					}
 					else if (e.op == OP_arg)
 					{
 						verbose.addFirst(e);
 						if (live.contains(e))
 							define(e,live,cg);
 					}
 					else
 					{
 						if (live.contains(e))
 						{
 							verboseStatus(" live Expr " + e);
 							define(e,live,cg);
 							if (e.onStack())
 							{
 								verboseStatus("  storing live Expr " + e + " from stack");
 								issue_store(e, m, out, verbose, stk);
 								verboseStatus("   recycling " + e);
 								in.add(e);
 							}
 							else if (e.inLocal())
 							{
 								verboseStatus("  issuing live Expr " + e);
 								issue_expr(e, m, out, verbose, stk, scp, live, locals);
 							}
 						}
 						else if (e.op == OP_xarg)
 						{
 							issue_pop(m,out,verbose,e);
 							verbose.addFirst(e);
 						}
 						else
 						{
 							verboseStatus(" non-live Expr " + e);
 							if (e.onStack())
 								issue_pop(m,out,verbose,e);
 							issue_expr(e, m, out, verbose, stk, scp, live, locals);
 						}
 					}
 				}
 			}
 			
 			fwd_state(m, locals, pred, liveout, stkout, scpout, work, b, live, stk, scp, verbose, out, phis);
 		}
 		verboseStatus("STK_LIVEOUT " + liveout);
 		verboseStatus("CONFLICTS " + cg);
 
 		for (Block b: code)
 		{
 			verboseStatus("");
 			verboseStatus(b);
 			for (Object o: listings.get(b)) 
 				if (o instanceof Expr) 
 					print((Expr)o); 
 				else 
 					verboseStatus(o);
 		}		
 		return cg;
 	}
 
 	void showstate(Set<Expr> live, Deque<Expr> stk, Deque<Expr> scp, Deque<Object> verbose)
 	{
 		verbose.addFirst("              live "+live + " stk " + stk);
 		if (!scp.isEmpty())
 			verbose.addFirst("              scp  "+scp);
 	}
 	
 	Expr remove_dup(Deque<Expr>stk, Method m, Deque<Expr>out, Deque<Object>verbose)
 	{
 		Expr e = stk.removeLast();
 		while (e == stk.peekLast())
 			issue_dup(stk.removeLast(), m, out, verbose);
 		return e;
 	}
 	
 	void try_dup(Deque<Expr>stk, Method m, Deque<Expr>out, Deque<Object>verbose)
 	{
 		if (stk.size()>2)
 		{
 			Expr e = stk.removeLast();
 			while (e == stk.peekLast())
 				issue_dup(stk.removeLast(), m, out, verbose);
 			stk.add(e);
 		}
 	}
 	
 	void issue_phi(Expr e, Deque<Object>verbose, Set<Expr>phis, Set<Expr>live, ConflictGraph cg)
 	{
 		verbose.addFirst(e);
 		phis.add(e);
 		if (live.contains(e))
 			for (Expr l: live)
 				if (l != e)
 					cg.add(l,e);
 	}
 
 	ConflictGraph sched_lazy(Method m, 
 			Deque<Block> code, 
 			Map<Integer,Integer> locals, 
 			SetMap<Block,Edge>pred,
 			Map<Block,Deque<Expr>> exprs,
 			ConflictGraph cg)
 	{
 		SetMap<Block,Expr> liveout = new SetMap<Block,Expr>();
 		Map<Block,Deque<Expr>> stkout = new TreeMap<Block,Deque<Expr>>();
 		Map<Block,Deque<Expr>> scpout = new TreeMap<Block,Deque<Expr>>();
 		HashMap<Block,Deque<Object>> listings = new HashMap<Block,Deque<Object>>();
 		
 		// low postorder numbers are first, 
 		PriorityQueue<Block> work = new PriorityQueue<Block>(code.size(),new Comparator<Block>() {
 			public int compare(Block b1,Block b2) {
 				return b1.postorder-b2.postorder;
 			}
 		});
 		
 		work.addAll(code);
 		while (!work.isEmpty())
 		{
 			Block b = work.remove();
 			while (work.peek() == b) 
 				work.remove(); // remove dups
 
 			Set<Expr> live = new TreeSet<Expr>();
 			Deque<Expr> in = new ArrayDeque<Expr>(b.exprs);
 			Deque<Expr> stk = new ArrayDeque<Expr>();
 			Deque<Expr> scp = new ArrayDeque<Expr>();
 			Deque<Object> verbose = new LinkedDeque<Object>();
 			Deque<Expr> out = new LinkedDeque<Expr>();
 
 			// if any succ has defined an expected stack, use it.
 			// if more than one have then they all have to match.
 			if (stkout.containsKey(b))
 				stk.addAll(stkout.get(b));
 
 			// merge the live vars from each succ
 			live.addAll(liveout.get(b));
 
 			for (Expr l: live)
 				locals.put(l.id, l.op == OP_arg ? l.imm[0] : -1);
 			
 			exprs.put(b,out);
 			listings.put(b,verbose);
 			
 			Set<Expr>phis = new TreeSet<Expr>();
 
 			while (!in.isEmpty())
 			{
 				showstate(live, stk, scp, verbose);
 				Expr e = in.removeLast();
 				
 				if (e.op == OP_phi)
 				{
 					issue_phi(e, verbose, phis, live, cg);
 					continue;
 				}
 				assert(phis.isEmpty());
 
 				boolean onstack = false;
 				while (stk.contains(e))
 				{
 					onstack = true;
 					Expr f = remove_dup(stk, m, out, verbose);
 					if (f != e || stk.contains(e))
 						issue_load(f,m,out,verbose,live,locals);
 				}
 				
 				if (e.op == OP_arg)
 				{
 					if (onstack)
 						issue_load(e,m,out,verbose,live,locals);
 					verbose.addFirst(e);
 					if (live.contains(e))
 						define(e,live,cg);
 				}
 				else
 				{
 					if (live.contains(e))
 					{
 						if (e.op == OP_xarg)
 							while (!stk.isEmpty())
 								loadTOS(m,stk,scp,out,verbose,live,locals);
 						define(e, live, cg);
 						if (onstack)
 							stk.add(e);
 						issue_store(e, m, out, verbose, stk);
 						in.add(e);
 					}
 					else if (e.op == OP_xarg)
 					{
 						if (!stk.isEmpty())
 						{
 							while (!stk.isEmpty())
 								loadTOS(m,stk,scp,out,verbose,live,locals);
 							in.add(e); // put xarg back
 							if (onstack)
 								stk.add(e); // make xarg the only thing on the stack.
 						}
 						else
 						{
 							if (!onstack)
 								issue_pop(m,out,verbose,e);
 							verbose.addFirst(e); //print but don't issue it.
 						}
 					}
 					else
 					{
 						if (!onstack && e.onStack())
 							issue_pop(m,out,verbose,e);
 						issue_expr(e, m, out, verbose, stk, scp, live, locals);
 					}
 				}
 			}
 			
 			fwd_state(m, locals, pred, liveout, stkout, scpout, work, b, live, stk, scp, verbose, out, phis);
 		}
 		verboseStatus("SCHED LIVEOUT " + liveout);
 		verboseStatus("SCHED STKOUT " + stkout);
 		verboseStatus("SCHED CONFLICTS " + cg);
 
 		for (Block b: code)
 		{
 			verboseStatus("");
 			verboseStatus(b);
 			for (Object o: listings.get(b)) 
 				if (o instanceof Expr) 
 					print((Expr)o); 
 				else 
 					verboseStatus(o);
 		}		
 		return cg;
 	}
 	
 	static boolean isThrowEdge(Edge e)
 	{
 		return e.handler != null;
 	}
 	
 	/** 
 	 * issue a load for whatever is on the stack top
 	 */
 	void loadTOS(Method m, Deque<Expr>stk, Deque<Expr>scp, Deque<Expr>out, Deque<Object>verbose, Set<Expr>live, Map<Integer,Integer>locals)
 	{
 		// stk doesn't match, make all successors expect nothing.
 		showstate(live, stk, scp, verbose);
 		Expr e = stk.removeLast();
 		if (e == stk.peekLast())
 			issue_dup(e, m, out, verbose);
 		else
 			issue_load(e, m, out, verbose, live, locals);
 	}
 
 	/**
 	 * forward the stack & live variable state from the top of the current
 	 * block (b) to the end of each predecessor block, translating phi nodes
 	 * at the same time.
 	 * 
 	 * @param m
 	 * @param locals
 	 * @param pred
 	 * @param liveout
 	 * @param stkout
 	 * @param work
 	 * @param b
 	 * @param live
 	 * @param stk
 	 * @param verbose
 	 * @param out
 	 * @param phis
 	 */
 	void fwd_state(Method m, Map<Integer, Integer> locals, 
 			SetMap<Block, Edge> pred, SetMap<Block, Expr> liveout, 
 			Map<Block, Deque<Expr>> stkout, Map<Block,Deque<Expr>> scpout,
 			PriorityQueue<Block> work, Block b, Set<Expr> live, 
 			Deque<Expr> stk, Deque<Expr> scp, 
 			Deque<Object> verbose, Deque<Expr> out, Set<Expr> phis)
 	{
 		// first prune stack to something safe to forward.
 		TreeMap<Block,Deque<Expr>>stkout2 = new TreeMap<Block,Deque<Expr>>(stkout);
 		
 		for (Edge p: pred.get(b))
 		{
 			Block f = p.from;
 			Deque<Expr> stk2 = clone_stk(phis,stk,p);
 			if (!stkout2.containsKey(f))
 			{
 				stkout2.put(f,stk2);
 			}
 			else
 			{
 				int prefix = stacks_equal(stk2,stkout2.get(f));
 				assert(stk.size() >= prefix);
 				while (stk.size() > prefix)
 					loadTOS(m,stk,scp,out,verbose,live,locals);
 			}
 		}
 		showstate(live, stk, scp, verbose);
 		
 		for (Edge p: pred.get(b))
 		{
 			Block f = p.from;
 
 			// forward live exprs
 			Set<Expr> live2 = clone_live(phis,live,p);
 			if (liveout.get(f).addAll(live2))
 				work.add(f);
 
 			// forward stack exprs
 			Deque<Expr> stk2 = clone_stk(phis,stk,p);
 			if (!stkout.containsKey(f))
 			{
 				stkout.put(f, stk2);
 			}
 			else
 			{
 				int prefix = stacks_equal(stk2,stkout.get(f));
 				assert(stk2.size() == prefix && stkout.get(f).size() >= prefix);
 				if (stkout.get(f).size() > prefix)
 				{
 					stkout.put(f, stk2);
 					work.add(f);
 					for (Edge s: f.succ())
 						if (s.to != b)
 							work.add(s.to);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * clone the live vars set for a particular predecessor edge,
 	 * replacing phi's with the expr corresponding to that edge.
 	 *
 	 * @param phis
 	 * @param live
 	 * @param p
 	 * @return
 	 */
 	Set<Expr> clone_live(Set<Expr>phis, Set<Expr>live, Edge p)
 	{
 		if (phis.isEmpty() || live.isEmpty())
 			return live;
 		Set<Expr> copy = new TreeSet<Expr>();
 		for (Expr e: live)
 			copy.add(phis.contains(e) ? e.args[findPhiArg(e, p)] : e);
 		return copy;
 	}
 	
 	/**
 	 * clone the stack layout for a predecessor edge, replacing phi's
 	 * with the argument coming from that edge.
 	 * 
 	 * @param phis
 	 * @param stk
 	 * @param p
 	 * @return
 	 */
 	Deque<Expr> clone_stk(Set<Expr>phis, Deque<Expr>stk, Edge p)
 	{
 		if (phis.isEmpty() || stk.isEmpty())
 			return stk;
 		Deque<Expr> copy = new ArrayDeque<Expr>();
 		for (Expr e: stk)
 			copy.add(phis.contains(e) ? e.args[findPhiArg(e, p)] : e);
 		return copy;
 	}
 	
 	int stacks_equal(Deque<Expr>stk1, Deque<Expr>stk2)
 	{
 		int i=0;
 		Iterator<Expr> i1 = stk1.iterator();
 		Iterator<Expr> i2 = stk2.iterator();
 		while (i1.hasNext() && i2.hasNext())
 		{
 			if (i1.next() != i2.next())
 				return i;
 			i++;
 		}
 		return i;
 	}
 	
 	void define(Expr e, Set<Expr> live, ConflictGraph cg)
 	{
 		verboseStatus("define() kills " + e.toString());
 		live.remove(e);
 		for (Expr l: live)
 			cg.add(e,l);
 	}
 	
 	void issue_expr(Expr e, Method m, Deque<Expr>out, Deque<Object> verbose, Deque<Expr>stk, Deque<Expr>scp, Set<Expr>live, Map<Integer,Integer> locals)
 	{
 		verboseStatus("issue_expr(" + formatExpr(e) + ")");
 		if (!e.isSynthetic())
 			out.addFirst(e);
 		verbose.addFirst(e);
 		for (Expr a: e.args)
 		{
 			verboseStatus(".. pushing " + formatExpr(a));
 			stk.add(a);
 		}
 		for (Expr a: e.locals)
 			use(a, live, locals);
 		try_dup(stk, m, out, verbose);
 	}
 	
 	void issue_store(Expr e, Method m, Deque<Expr>out, Deque<Object> verbose, Deque<Expr>stk)
 	{
 		Expr set = setlocal(m, e.id, e);
 		verboseStatus("issue_store(" + formatExpr(set) + ")");
 		out.addFirst(set);
 		verbose.addFirst(set);
 		verboseStatus(".. pushing " + formatExpr(e));
 		stk.add(e);
 	}
 	
 	void issue_dup(Expr e, Method m, Deque<Expr>out, Deque<Object> verbose)
 	{
 		Expr dup = dup(m,e);
 		out.addFirst(dup);
 		verboseStatus("issue_dup(" + dup.toString() + ")");
 		verbose.addFirst(dup);
 	}
 	
 	void issue_pop(Method m, Deque<Expr>out, Deque<Object> verbose, Expr a)
 	{
 		Expr pop = new Expr(m, OP_pop, a);
 		out.addFirst(pop);
 		verbose.addFirst(pop);
 	}
 
 	void issue_load(Expr e, Method m, Deque<Expr>out, Deque<Object>verbose, Set<Expr>live, Map<Integer,Integer>locals)
 	{
 		use(e, live, locals);
 		Expr get = getlocal(m,e.id);
 		out.addFirst(get);
 		verboseStatus("issue_load(" + formatExpr(get) + ")");
 		verbose.addFirst(get);
 	}
 
 	void use(Expr e, Set<Expr> live, Map<Integer, Integer> locals)
 	{
 		verboseStatus("use() enlivining " + formatExpr(e));
 		live.add(e);
 		locals.put(e.id, e.op == OP_arg ? e.imm[0] : -1);
 	}
 	
 	void rename(Expr e, Expr[] args, Map<Expr,Expr> map, EdgeMap<Expr> ssaSucc)
 	{
 		for (int i=0, n=args.length; i<n; i++)
 		{
 			Expr a = args[i];
 			while (map.containsKey(a))
 			{
 				ssaSucc.get(a).remove(e);
 				a = args[i] = map.get(a);
 				ssaSucc.get(a).add(e);
 			}
 		}
 	}
 	
 	/**
 	 * Copy propagation.
 	 * 
 	 * Phi nodes that only choose themselves or one other variable
 	 * are replaced by references to that other variable:
 	 * 
 	 *    @i = phi(@i ... @j), op(@i)   =>    = op(@j)
 	 *
 	 * additionally, references to a dup are replaced by references to the
 	 * dup's argument.  Dup is a copy instruction inserted by some previous
 	 * optimization. 
 	 * 
 	 *    @i = dup(@j), op(@i)   =>  op(@j)
 	 * 
 	 * We take advantage of SSA's sparseness by propagating changes along
 	 * def->use edges and iterating until our worklist is empty.
 	 * 
 	 * This pass also removes any phi arguments that are dead, i.e. if the
 	 * edge contributing the argument is from an unreachable block.  This
 	 * may turn the phi into a copy which is subsequently removed.
 	 */
 	void cp(Deque<Block> code)
 	{
 		EdgeMap<Expr> uses = findUses(code);
 		Map<Expr,Expr> map = new HashMap<Expr,Expr>();
 		Set<Expr> work = new WorkSet<Expr>();
 		for (Block b: code)
 		{
 			for (Expr e: b)
 				if (e.op == OP_phi || e.op == OP_dup)
 					work.add(e);
 		}
 		
 		while (!work.isEmpty())
 		{
 			Expr e = remove(work);
 			rename(e, e.args, map, uses);
 			rename(e, e.scopes, map, uses);
 			rename(e, e.locals, map, uses);
 			if (e.op == OP_dup)
 			{
 				map.put(e, e.locals[0]);
 				work.addAll(uses.get(e));
 			}
 			else if (e.op == OP_phi)
 			{
 				assert(e.args.length == e.pred.length);
 				for (int j=e.pred.length-1; j >= 0; j--)
 					if (!code.contains(e.pred[j].from))
 						e.remove(j);
 				Expr a = null;
 				for (int j=e.pred.length-1; j >= 0; j--)
 				{
 					if (e.args[j] != e && e.args[j] != a)
 						if (a == null)
 							a = e.args[j];
 						else
 						{
 							a = null;
 							break;
 						}
 				}
 				if (a != null && map.get(e) != a)
 				{
 					map.put(e, a);
 					work.addAll(uses.get(e));
 					e.clearEffect();
 				}
 			}
 		}
 	}
 
 	boolean hasSideEffect(Expr e)
 	{
 		return e.isPx() || e.hasEffect();
 	}
 	
 	void dfs_visit_el(Edge[] el, BitSet visited, Deque<Block>list)
 	{
 		for (int i=el.length-1; i >= 0; i--)
 			dfs_visit(el[i].to, visited, list);
 	}
 	
 	Deque<Block> dfs_visit(Block b, BitSet visited, Deque<Block>list)
 	{
 		if (!visited.get(b.id))
 		{
 			visited.set(b.id);
 			
 			dfs_visit_el(b.xsucc, visited, list);
 			dfs_visit_el(b.succ(), visited, list);
 
 			b.postorder = list.size();
 			list.addFirst(b);
 		}
 		return list;
 	}
 	
 	Deque<Block> dfs(Block entry)
 	{
 		return dfs_visit(entry, new BitSet(), new LinkedDeque<Block>());
 	}
 	
 	void schedule_loop(Block b, EdgeMap<Block> loops, Deque<Block> done)
 	{
 		Set<Block> loop = loops.get(b);
 		for (Block lb: dfs(b))
 		{
 			if (!done.contains(lb) && loop.contains(lb))
 			{
 				done.add(lb);
 				if (loops.containsKey(lb))
 					schedule_loop(lb, loops, done);
 			}
 		}
 	}
 	
 	/**
 	 * layout blocks.  Normal forward flow is laid out in reverse postorder.
 	 * Loops are handled specially to keep blocks in the loop body close to
 	 * each other.  loop exit blocks are postponed until after the loop.
 	 * @param entry
 	 * @return
 	 */
 	Deque<Block> schedule(Block entry)
 	{
 		Deque<Block>code = dfs(entry);
 		Deque<Block>done = new ArrayDeque<Block>();
 		SetMap<Block,Edge> pred = preds(code);
 		Map<Block,Block> idom = idoms(code,pred);
 		EdgeMap<Block> loops = findLoops(code,idom,pred);
 		if (!loops.isEmpty())
 			verboseStatus("LOOPS "+loops);
 		
 		for (Block b: code)
 		{
 			if (!done.contains(b))
 				done.add(b);
 			if (loops.containsKey(b))
 				schedule_loop(b, loops, done);
 		}
 		
 		code.clear();
 		code.addAll(done);
 		while (code.size()>1)
 		{
 			Block b = code.removeFirst();
 			Expr last = b.last();
 			Block next = code.peekFirst();
 			if (isBranch(last) && last.succ[0].to != next &&
 					last.succ[1].to == next)
 				invert(last);
 		}
 		return done;
 	}
 	
 	boolean isJump(Expr e)
 	{
 		return e.op == OP_jump;
 	}
 	
 	boolean isBranch(Expr e)
 	{
 		return e.succ != null && e.succ.length == 2 && e.op != OP_lookupswitch;
 	}
 	
 	/**
 	 * find the nearest common dominator of b1 and b2
 	 *
 	 * @param b1
 	 * @param b2
 	 * @param doms
 	 * @return
 	 */
 	Block intersect(Block b1, Block b2, Block[] doms)
 	{
 		while (b1 != b2)
 		{
 			while (b1.postorder < b2.postorder)
 				b1 = doms[b1.postorder];
 			while (b2.postorder < b1.postorder)
 				b2 = doms[b2.postorder];
 		}
 		return b1;
 	}
 	
 	/**
 	 * compute idom(b) for each b in the flow graph using the
 	 * classic iterative algorithm with the dom set formed by
 	 * the list of idom(b) from b to entry, as presented by
 	 * Cooper, Harvey, and Kennedy:
 	 * http://citeseer.ist.psu.edu/cooper01simple.html
 	 * @param entry
 	 * @return
 	 */
 	Map<Block,Block> idoms(Deque<Block> all, SetMap<Block,Edge>pred)
 	{
 		Block entry = all.peekFirst();
 		Block[] doms = new Block[entry.postorder+1];
 		
 		doms[entry.postorder] = entry;
 		boolean changed;
 		do
 		{
 			changed = false;
 			for (Block b: all)
 			{
 				if (b == entry)
 					continue;
 				Block new_idom = null;
 				// pick any pred that is processed already
 				for (Edge e: pred.get(b))
 				{
 					Block p = e.from;
 					if (doms[p.postorder] != null)
 					{
 						new_idom = p;
 						break;
 					}
 				}
 				// intersect with all other processed preds
 				for (Edge e: pred.get(b))
 				{
 					Block p = e.from;
 					if (p != new_idom && doms[p.postorder] != null)
 						new_idom = intersect(p, new_idom, doms);
 				}
 				// if new dominator found then do another pass
 				if (doms[b.postorder] != new_idom)
 				{
 					doms[b.postorder] = new_idom;
 					changed = true;
 				}
 			}
 		}
 		while (changed);
 		
 		Map<Block,Block> map = new TreeMap<Block,Block>();
 		for (Block b: all)
 			if (b != entry)
 				map.put(b, doms[b.postorder]);
 		
 		return map;
 	}
 	
 	boolean dominates(Block p, Block s, Map<Block,Block>idom)
 	{
 		for (Block b = s; b != null; b = idom.get(b))
 			if (b == p)
 				return true;
 		return false;
 	}
 	
 	/**
 	 * returns true if P->S is a loop back edge.  This is the
 	 * case if S is visited first in reverse postorder and S
 	 * dominates P.
 	 * 
 	 * @param p
 	 * @param s
 	 * @param idom
 	 * @return
 	 */
 	boolean isLoop(Edge e, Map<Block,Block>idom)
 	{
 		return e.isBackedge() && dominates(e.to, e.from, idom);
 	}
 	
 	Block remove(Set<Block>work)
 	{
 		Iterator<Block> i = work.iterator();
 		Block b = i.next();
 		i.remove();
 		return b;
 	}
 	
 	Expr remove(Set<Expr>work)
 	{
 		Iterator<Expr> i = work.iterator();
 		Expr e = i.next();
 		i.remove();
 		return e;
 	}
 	
 	Edge remove(Set<Edge>work)
 	{
 		Iterator<Edge> i = work.iterator();
 		Edge e = i.next();
 		i.remove();
 		return e;
 	}
 	
 	Method remove(List<Method> list)
 	{
 		return list.remove(list.size()-1);
 	}
 	
 	EdgeMap<Block> findLoops(Deque<Block> code)
 	{
 		SetMap<Block,Edge> pred = preds(code);
 		return findLoops(code, idoms(code, pred), pred);
 	}
 	
 	/**
 	 * loop code layout
 	 * the loop is defined by the back edge from T->H.  H (header) is the source
 	 * of the back edge.  We want H at the bottom of the loop and T at the top.
 	 * Lay the blocks out in DFS order starting with T and skipping any blocks
 	 * that aren't part of the loop.
 	 * 
 	 * prefer to have fall-through blocks that are part of the loop.
 	 */
 	
 	EdgeMap<Block> findLoops(Deque<Block> code, Map<Block,Block>idom, SetMap<Block,Edge>pred)
 	{
 		EdgeMap<Block> loops = new EdgeMap<Block>();
 		for (Block b: code)
 			for (Edge s: b.succ())
 				if (isLoop(s, idom))
 				{
 					verboseStatus("backedge "+s);
 					Block h = s.to;
 					// find the set of blocks that are in the loop body.
 					Set<Block> loop = loops.get(h);
 					Set<Block> work = new WorkSet<Block>();
 					for (Edge p: pred.get(h))
 					{
 						if (isLoop(p,idom) && !loop.contains(p.from) && p.from != h)
 						{
 							loop.add(p.from);
 							work.add(p.from);
 						}
 					}
 					while (!work.isEmpty())
 					{
 						Block x = remove(work);
 						for (Edge p: pred.get(x))
 						{
 							if (p.from != h && !loop.contains(p.from))
 							{
 								loop.add(p.from);
 								work.add(p.from);
 							}
 						}
 					}
 				}
 		return loops;
 	}
 		
 	void dce_mark(BitSet used, Expr e)
 	{
 		if (used.get(e.id))
 			return;
 		used.set(e.id);
 		for (Expr a: e.args) dce_mark(used, a);
 		for (Expr a: e.scopes) dce_mark(used, a);
 		for (Expr a: e.locals) dce_mark(used, a);
 	}
 
 	/**
 	 * Dead Code Elimination.  First perform copy propagation and then
 	 * remove any expressions not used by the essential expressions
 	 * that have side effects.  
 	 * @param m
 	 */
 	void dce(Method m)
 	{
 		Deque<Block> code = dfs(m.entry.to);
 		
 		cp(code);
 		
 		BitSet marked = new BitSet();
 		for (Block b: code)
 			for (Expr e: b)
 				if (hasSideEffect(e))
 					dce_mark(marked,e);
 		
 		for (Block b: code)
 			for (Iterator<Expr> i = b.iterator(); i.hasNext();)
 				if (!marked.get(i.next().id))
 					i.remove();
 	}
 		
 	class Block implements Iterable<Expr>, Comparable<Block>, Indexable
 	{
 		Deque<Expr> exprs = new ArrayDeque<Expr>();
 		int id;
 		int postorder; // postorder number
 		Edge[] xsucc = noedges; // in-scope handlers for this block.
 		
 		Block(Method m)
 		{
 			this.id = m.blockId++;
 		}
 		
 		public int id()
 		{
 			return id;
 		}
 		
 		public String toString()
 		{ 
 			return 'B'+String.valueOf(id); 
 		}
 		
 		Expr first()
 		{
 			return exprs.peekFirst();
 		}
 		
 		Expr last()
 		{
 			return exprs.peekLast();
 		}
 		
 		Edge[] succ()
 		{
 			return last().succ;
 		}
 		
 		public Iterator<Expr> iterator()
 		{
 			return exprs.iterator();
 		}
 		
 		void add(Expr e)
 		{
 			exprs.add(e);
 		}
 		
 		/*void add(int i, Expr e)
 		{
 			exprs.add(i, e);
 		}*/
 		
 		void addAll(Block b)
 		{
 			exprs.addAll(b.exprs);
 		}
 		
 		boolean isEmpty()
 		{
 			return exprs.isEmpty();
 		}
 		
 		int size()
 		{
 			return exprs.size();
 		}
 		
 		/*Expr get(int i)
 		{
 			return exprs.get(i);
 		}*/
 		
 		void remove(Expr e)
 		{
 			exprs.remove(e);
 		}
 		
 		public int compareTo(Block b)
 		{
 			return this.id - b.id;
 		}
 	}
 	
 	String format(char op, Object[] a, char cp)
 	{
 		if (a == null)
 			return "";
 		StringBuilder s = new StringBuilder();
 		s.append(op);
 		for (Object o: a)
 			s.append(o).append(' ');
 		if (a.length > 0)
 			s.setCharAt(s.length()-1,cp);
 		else
 			s.append(cp);
 		return s.toString();
 	}
 	
 	static final Expr[] noexprs = new Expr[] {};
 	static final Edge[] noedges = new Edge[] {};
 	
 	static interface Indexable
 	{
 		int id();
 	}
 
 	static class Expr implements Comparable<Expr>, Indexable
 	{
 		int op;
 		Expr[] args = noexprs;   // args taken from operand stack
 		Expr[] scopes = noexprs; // args taken from scope stack
 		Expr[] locals = noexprs; // args taken from local variables
 		int[] imm;
 		Edge[] pred=noedges; // phi nodes only
 		Edge[] succ; // branch nodes only
 		int id;
 		int flags;
 		Name ref;
 		Object value; // only if pushconst
 		Type c; // only if OP_newclass
 		Method m; // only if OP_newfunction | callstatic
 
 		Expr(Method m, int op)
 		{
 			this.op = op;
 			flags = flagTable[op];
 			id = m.exprId++;
 		}
 		
 		Expr(Method m, int op, int imm1)
 		{
 			this(m,op);
 			this.imm = new int[] { imm1 };
 		}
 
 		Expr(Method m, int op, Object value)
 		{
 			this(m, op);
 			this.value = value;
 		}
 		
 		Expr(Method m, int op, Expr arg)
 		{
 			this(m, op);
 			args = new Expr[] { arg };
 		}
 
 		Expr(Method m, int op, Expr[] frame, int sp, int argc)
 		{
 			this(m, op);
 			args = capture(frame, sp, argc);
 		}
 
 		Expr(Method m, int op, int imm1, Expr[] frame, int sp, int argc)
 		{
 			this(m,op,frame,sp,argc);
 			this.imm = new int[] { imm1 };
 		}
 
 		Expr(Method m, int op, Name ref, Expr[] frame, int sp, int argc)
 		{
 			this(m, op,frame,sp,argc);
 			this.ref = ref;
 		}
 
 		Expr(Method m, int op, Name ref, Expr arg)
 		{
 			this(m, op);
 			this.ref = ref;
 			this.args = new Expr[] { arg };
 		}
 		
 		public int id()
 		{
 			return id;
 		}
 		
 		void append(Expr a, Edge p)
 		{
 			args = copyOf(args, args.length+1);
 			args[args.length-1] = a;
 
 			pred = copyOf(pred, pred.length+1);
 			pred[pred.length-1] = p;
 		}
 		
 		void remove(int j)
 		{
 			Expr[] a = new Expr[args.length-1];
 			System.arraycopy(args, 0, a, 0, j);
 			System.arraycopy(args, j+1, a, j, args.length-j-1);
 			args = a;
 			
 			Edge[] ed = new Edge[pred.length-1];
 			System.arraycopy(pred, 0, ed, 0, j);
 			System.arraycopy(pred, j+1, ed, j, pred.length-j-1);
 			pred = ed;
 		}
 		
 		public String toString()
 		{
 			return (onStack() ? "t": onScope() ? "s" : inLocal() ? "l" : "i")+id;
 		}
 
 		void clearEffect()
 		{
 			flags &= ~EFFECT;
 		}
 		
 		void clearPx()
 		{
 			flags &= ~PX;
 		}
 		
 		void setPure()
 		{
 			clearEffect();
 			clearPx();
 		}
 		
 		boolean hasEffect()
 		{
 			return (flags & EFFECT) != 0;
 		}
 		
 		boolean isPx()
 		{
 			return (flags & PX) != 0;
 		}
 
 		boolean isSynthetic()
 		{
 			return (flags & SYNTH) != 0;
 		}
 		
 		boolean onStack()
 		{
 			return (flagTable[op] & STKVAL) != 0;
 		}
 
 		boolean isOper()
 		{
 			return (flagTable[op] & OPER) != 0;
 		}
 		
 		boolean onScope()
 		{
 			return (flagTable[op] & SCPVAL) != 0;
 		}
 		
 		boolean inLocal()
 		{
 			return (flagTable[op] & LOCVAL) != 0;
 		}
 
 		boolean isCoerce()
 		{
 			return (flagTable[op] & COERCE) != 0;
 		}
 
 		public int compareTo(Expr other)
 		{
 			assert(this.id != other.id || this == other);
 			return this.id - other.id;
 		}
 	}
 	
 	static Type[] notypes = new Type[] {};
 	static Typeref[] notyperefs = new Typeref[] {};
 	
 	class Typeref
 	{
 		final Type t;
 		final boolean nullable;
 		
 		Typeref(Type t, boolean nullable)
 		{
 			assert(t != null);
 			this.t = t;
 			this.nullable = nullable;
 		}
 		
 		Typeref nonnull()
 		{
 			return nullable ? new Typeref(t,false) : this;
 		}
 		
 		public boolean equals(Object o)
 		{
 			return (o instanceof Typeref) && ((Typeref)o).t == t && ((Typeref)o).nullable == nullable;
 		}
 		
 		Binding find(Name n)
 		{
 			return t.find(n);
 		}
 		
 		Binding findGet(Name n)
 		{
 			return t.findGet(n);
 		}
 		
 		public String toString()
 		{
 			return !t.ref.nullable || t==NULL || t==VOID || t==ANY ? t.toString() :
 					nullable ? t.toString() + "?" :
 					t.toString();
 		}
 	}
 	
 	final static int CTYPE_VOID			= 0;
 	final static int CTYPE_ATOM			= 1;
 	final static int CTYPE_BOOLEAN		= 2;
 	final static int CTYPE_INT			= 3;
 	final static int CTYPE_UINT			= 4;
 	final static int CTYPE_DOUBLE		= 5;
 	final static int CTYPE_STRING		= 6;
 	final static int CTYPE_NAMESPACE	= 7;
 	final static int CTYPE_OBJECT		= 8;
 
 	class Type
 	{
 		Name name;
 		Type base;
 		Type[] interfaces = notypes;
 		Symtab<Binding> defs;
 		Method init;
 		Type itype;
 		int flags;
 		Namespace protectedNs;
 		Typeref[] scopes = notyperefs; // captured outer scopes
 		boolean numeric;
 		boolean primitive;
 		boolean atom;
 		Object defaultValue = NULL;
 		Typeref ref;
 		int size;
 		int slotCount;
 		int ctype;
 		boolean obscure_natives;
 		
 		Type()
 		{
 			ref = new Typeref(this, true);
 			this.obscure_natives = false;
 			this.ctype = CTYPE_OBJECT;
 		}
 		
 		Type(Name name, Type base)
 		{
 			this();
 			this.name = name;
 			this.base = base;
 			this.defs = new Symtab<Binding>();
 			this.obscure_natives = false;
 		}
 		
 		boolean emitAsAny()
 		{
 			// not sure about this, attempting to coerce native class as *
 			return base == null && this != VOID && defs.size() == 0;
 		}
 		
 		public String toString()
 		{
 			return String.valueOf(name);
 		}
 		
 		boolean isFinal()
 		{
 			return (flags & CLASS_FLAG_final) != 0;
 		}
 		
 		void setFinal()
 		{
 			flags |= CLASS_FLAG_final;
 		}
 		
 		Binding find(Name n)
 		{
 			// look up the inheritance tree
 			for (Type t = this; t != null; t = t.base)
 			{
 				Binding b = t.defs.get(n);
 				if (b != null)
 					return b;
 			}
 			return null;
 		}
 		
 		Binding findGet(Name n)
 		{
 			Binding first = find(n);
 			if (first != null && isSetter(first))
 			{
 				if (first.peer != null)
 					return first.peer;
 				Binding second;
 				if (base != null && isGetter(second=base.findGet(n)))
 					return second;
 			}
 			return first;
 		}
 		
 		Binding findSlot(int slot)
 		{
 			for (Binding b: defs.values())
 			{
 				if (isSlot(b) && b.slot == slot)
 					return b;
 			}
 			return null;
 		}
 		
 		boolean hasProtectedNs()
 		{
 			return (flags & CLASS_FLAG_protected) != 0;
 		}
 	}
 	
 	static boolean isSlot(Binding b)
 	{
 		if (b == null) return false;
 		int tk = b.kind();
 		return tk == TRAIT_Var || tk == TRAIT_Const || tk == TRAIT_Class;
 	}
 	static boolean isConst(Binding b)
 	{
 		if (b == null) return false;
 		int tk = b.kind();
 		return tk == TRAIT_Const || tk == TRAIT_Getter;
 	}
 	static boolean isClass(Binding b)
 	{
 		if (b == null) return false;
 		int tk = b.kind();
 		return tk == TRAIT_Class;
 	}
 	static boolean isMethod(Binding b)
 	{
 		if (b == null) return false;
 		int tk = b.kind();
 		return tk == TRAIT_Method;
 	}
 	static boolean isGetter(Binding b)
 	{
 		if (b == null) return false;
 		int tk = b.kind();
 		return tk == TRAIT_Getter;
 	}
 	static boolean isSetter(Binding b)
 	{
 		if (b == null) return false;
 		int tk = b.kind();
 		return tk == TRAIT_Setter;
 	}
 	
 	/**
 	 * how many stack elements need to be popped to complete
 	 * the given multiname reference.
 	 */
 	static int[] refArgc = 
 	{
 	    0,//byte CONSTANT_Void = 0x00;  // not actually interned
 	    0,//byte CONSTANT_Utf8 = 0x01;
 	    0, //0x02
 	    0,//byte CONSTANT_Integer = 0x03;
 	    0,//byte CONSTANT_UInteger = 0x04;
 	    0,//byte CONSTANT_PrivateNamespace = 0x05;
 	    0,//byte CONSTANT_Double = 0x06;
 	    0,//byte CONSTANT_Qname = 0x07;  // ns::name, const ns, const name
 	    0,//byte CONSTANT_Namespace = 0x08;
 	    0,//byte CONSTANT_Multiname = 0x09;    //[ns...]::name, const [ns...], const name
 	    0,//byte CONSTANT_False = 0x0A;
 	    0,//byte CONSTANT_True = 0x0B;
 	    0,//byte CONSTANT_Null = 0x0C;
 	    0,//byte CONSTANT_QnameA = 0x0D;    // @ns::name, const ns, const name
 	    0,//byte CONSTANT_MultinameA = 0x0E;// @[ns...]::name, const [ns...], const name
 	    1,//byte CONSTANT_RTQname = 0x0F;    // ns::name, var ns, const name
 	    1,//byte CONSTANT_RTQnameA = 0x10;    // @ns::name, var ns, const name
 	    2,//byte CONSTANT_RTQnameL = 0x11;    // ns::[name], var ns, var name
 	    2,//byte CONSTANT_RTQnameLA = 0x12; // @ns::[name], var ns, var name
 	    0,//0x13
 	    0,//0x14
 	    0,//byte CONSTANT_Namespace_Set = 0x15; // a set of namespaces - used by multiname
 	    0,//byte CONSTANT_PackageNamespace = 0x16; // a namespace that was derived from a package
 	    0,//byte CONSTANT_PackageInternalNs = 0x17; // a namespace that had no uri
 	    0,//byte CONSTANT_ProtectedNamespace = 0x18;
 	    0,//byte CONSTANT_ExplicitNamespace = 0x19;
 	    0,//byte CONSTANT_StaticProtectedNs = 0x1A;
 	    1,//byte CONSTANT_MultinameL = 0x1B;
 	    1,//byte CONSTANT_MultinameLA = 0x1C;
 	};
 	
 	static class FrameState
 	{
 		Expr[] frame;
 		int sp;
 		int scopep;
 		
 		public FrameState(Expr[] frame, int sp, int scopep)
 		{
 			super();
 			this.frame = new Expr[frame.length];
 			this.sp = sp;
 			this.scopep = scopep;
 		}
 	}
 	
 	static boolean isLive(int i, Method m, int scopep)
 	{
 		return i < scopep || i >= m.local_count + m.max_scope;
 	}
 	
 	Block createBlock(Method m, Edge edge, Map<Block,FrameState>states, Expr[] frame, int sp, int scopep)
 	{
 		Block b = new Block(m);
 		FrameState state = new FrameState(frame,sp,scopep);
 		if (edge != null)
 			edge.to = b;
 		for (int i=0; i < sp; i++)
 		{
 			if (isLive(i,m,scopep) && frame[i] != null)
 			{
 				Expr e = new Expr(m,OP_phi);
 				if (edge != null)
 				{
 					e.args = new Expr[] { frame[i] };
 					e.pred = new Edge[] { edge };
 				}
 				b.add(state.frame[i] = e);
 			}
 		}
 		states.put(b, state);
 		return b;
 	}
 	
 	/**
 	 * merge state with block at position pos.  If the target block
 	 * doesn't exist, create it first.
 	 * 
 	 * @param m
 	 * @param edge
 	 * @param blocks
 	 * @param states
 	 * @param pos
 	 * @param frame
 	 * @param sp
 	 * @param scopep
 	 */
 	void merge(Method m, Edge edge, Map<Integer,Block> blocks, Map<Block,FrameState> states, int pos, Expr[] frame, int sp, int scopep)
 	{
 		if (!blocks.containsKey(pos))
 		{
 			Block b = createBlock(m,edge,states,frame,sp,scopep);
 			blocks.put(pos, b);
 		}
 		else if (edge != null)
 		{
 			edge.to = blocks.get(pos);
 			merge(m, edge, states, frame, sp, scopep);
 		}
 	}
 	
 	/**
 	 * merge current state with existing block (edge.to)
 	 * @param m
 	 * @param edge
 	 * @param states
 	 * @param frame
 	 * @param sp
 	 * @param scopep
 	 */
 	void merge(Method m, Edge edge, Map<Block,FrameState>states, Expr[] frame, int sp, int scopep)
 	{
 		FrameState target = states.get(edge.to);
 		assert(target.sp == sp);
 		assert(target.scopep == scopep);
 		for (int i=0; i < sp; i++)
 		{
 			if (isLive(i,m,scopep) && frame[i] != target.frame[i])
 			{
 				assert(frame[i] != null && target.frame[i].op == OP_phi);
 				target.frame[i].append(frame[i], edge);
 			}
 		}
 	}
 
 	void xmerge(Method m, Edge edge, Map<Integer,Block> blocks, Map<Block,FrameState> states, int pos, Expr[] frame, int sp, int scopep)
 	{
 		scopep = m.local_count;
 		sp = scopep + m.max_scope;
 		
 		Handler h = edge.handler;
 		
 		if (h.entry == null)
 		{
 			// first time, create handler block that jumps to catch block
 			Block hb = h.entry = createBlock(m, edge, states, frame, sp, scopep);
 			Expr xarg, jump;
 			hb.add(xarg = new Expr(m,OP_xarg, edge.label));
 			hb.add(jump = new Expr(m,OP_jump));
 			jump.succ = new Edge[] { new Edge(m, hb, 0, blocks.get(pos)) };
 			Expr save = frame[sp];
 			frame[sp] = xarg;
 			
 			// and merge the handler block with the real exception target.
 			merge(m, jump.succ[0], blocks, states, pos, frame, sp+1, scopep);
 			frame[sp] = save;
 		}
 		else
 		{
 			// each time, merge current state with handler block state.
 			edge.to = h.entry;
 			merge(m, edge, states, frame, sp, scopep);
 		}
 	}
 	
 	static Expr[] capture(Expr[] frame, int top, int len)
 	{
 		Expr[] args = new Expr[len];
 		System.arraycopy(frame, top-len, args, 0, len);
 		return args;
 	}
 	
 	boolean isPx(int op)
 	{
 		return (flagTable[op] & PX) != 0;
 	}
 	
 	
 	void print(Expr e)
 	{
 		if ( !verbose_mode )
 			return;
 		PrintWriter pw = new PrintWriter(System.out);
 		printssa(e, pw);
 		pw.flush();
 	}
 	
 	void printabc(Expr e, PrintWriter out)
 	{
 		if ( !verbose_mode )
 			return;
 		out.print("    " + opNames[e.op]);
 		if (e.imm != null)
 		{
 			StringBuilder s = new StringBuilder();
 			s.append('<');
 			for (int i: e.imm)
 				s.append(i).append(',');
 			s.setCharAt(s.length()-1,'>');
 			out.print(s);
 		}
 		if (e.succ != null)
 			out.print(format('[',e.succ,']'));
 		if (e.value != null)
 		{
 			out.print(" ");
 			print(e.value, out);
 		}
 		if (e.ref != null)
 			out.print(" "+e.ref);//.format()); // full name
 			
 		out.println();
 	}
 	
 	void printssa(Expr e, PrintWriter out)
 	{		
 		out.println(formatExpr(e));
 	}
 	
 	String formatExpr(Expr e)
 	{
 		if ( null == e )
 		{
 			return "null";
 		}
 		
 		StringBuffer outBuffer = new StringBuffer();
 		
 		outBuffer.append(e.toString());
 		if (e.onStack() || e.inLocal() || e.onScope())
 			outBuffer.append(" =");
 		else
 			outBuffer.append("  ");
 			
 		if (e.value == null)
 			outBuffer.append(" "+opNames[e.op]);
 		if (e.imm != null)
 		{
 			outBuffer.append('<');
 			for (int i: e.imm)
 				outBuffer.append(i).append(',');
 			outBuffer.setCharAt(outBuffer.length()-1,'>');
 		}
 		if (e.args.length > 0)
 			outBuffer.append(format('(',e.args,')'));
 		if (e.locals.length > 0)
 			outBuffer.append(format('(',e.locals,')'));
 		if (e.scopes.length > 0)
 			outBuffer.append(format('{',e.scopes,'}'));
 		if (e.pred.length>0)
 			outBuffer.append(format('[',e.pred,']'));
 		if (e.succ != null)
 			outBuffer.append(format('[',e.succ,']'));
 		if (e.value != null)
 			outBuffer.append(formatObject(e.value));
 		if (e.ref != null)
 			outBuffer.append(" "+e.ref);//.format()); // full name
 	
 		return outBuffer.toString();
 	}
 	
 	void print(Object value, PrintWriter out)
 	{
 		out.print(formatObject(value));
 	}
 	
 	String formatObject(Object value)
 	{
 		if (value instanceof String)
 			return(" \"" + ((String)value).replace("\n","\\n").replace("\r","\\r") + "\"");
 		else
 			return(" "+ value);
 	}
 	
 	void print(Deque<Block> blocks)
 	{
 		if ( ! verbose_mode )
 			return;
 		verboseStatus(blocks);
 		PrintWriter pw = new PrintWriter(System.out);
 		for (Block b: blocks)
 			print(b,pw);
 		pw.flush();
 	}
 	void printabc(Deque<Block> blocks)
 	{
 		if ( ! verbose_mode )
 			return;
 		verboseStatus(blocks);
 		PrintWriter pw = new PrintWriter(System.out);
 		for (Block b: blocks)
 			printabc(b,pw);
 		pw.flush();
 	}
 	
 	void print(Block b, PrintWriter pw)
 	{
 		pw.println();
 		printssa(b, pw);
 	}
 	
 	void printabc(Block b, PrintWriter out)
 	{
 		out.println();
 		out.println(b);
 		if (b.xsucc.length > 0)
 			out.println(Arrays.toString(b.xsucc));
 		for (Expr s: b)
 			printabc(s,out);
 	}
 
 	void printssa(Block b, PrintWriter out)
 	{
 		out.println(b);
 		if (b.xsucc.length > 0)
 			out.println(Arrays.toString(b.xsucc));
 		for (Expr s: b)
 			printssa(s,out);
 	}
 		
 	void dot(String suffix, Method m)
 	{
 		// don't bother with single-block methods.
 		if (m.entry.to.succ().length == 0)
 			return;
 		
 		// save a dot file for the method
 		try
 		{
 			PrintWriter out = new PrintWriter(new FileWriter(m.name+suffix+".dot"));
 			try
 			{
 				Deque<Block> code = dfs(m.entry.to);
 				
 				out.println("digraph {");
 				out.println("compound=true;");
 				out.println("label=\""+m.name+suffix+"\";");
 				out.println("labelloc=top;");
 				out.println("fontsize=10;");
 				
 				if (SHOW_DFG)
 				{
 					out.println("ranksep=.1; nodesep=.1;");
 					out.println("node [shape=plaintext,width=.05,height=.05,fontsize=7];");
 				}
 				else
 				{
 					out.println("ranksep=.25; nodesep=.25;");
 					out.println("node [shape=box,width=.1,height=.1,fontsize=7];");
 				}
 					
 				out.println("edge [arrowsize=.5,fontsize=8,labelfontsize=8];");
 				
 				for (Block b: code)
 					if (SHOW_DFG)
 						dot_dfg(b,out);
 					else
 						dot(b,out);
 				
 				Map<Block,Block> doms = idoms(code,allpreds(code));
 				out.println("node [shape=box];");
 				out.println("subgraph cluster1 { label=\"Dominators\"; color=white; ");
 				for (Block b: dfs(m.entry.to))
 				{
 					out.println("D"+b+" [label="+b+"];");
 					if (doms.containsKey(b))
 						out.println("D"+doms.get(b)+" -> D"+b);
 				}
 				out.println("}");
 				
 				out.println("}");
 			}
 			finally
 			{
 				out.close();
 			}
 		}
 		catch (IOException e) 
 		{
 			throw new RuntimeException(e);
 		}
 	}
 	
 	static class LabelWriter extends PrintWriter
 	{
 		StringWriter w;
 		LabelWriter(StringWriter w)
 		{
 			super(w);
 			this.w = w;
 		}
 		public void println() { print("\\l"); flush(); }
 		public void print(String s) 
 		{
 			super.print(s.replace("\"","''").replace("\u0278","&phi;")); 
 		}
 		public String toString()
 		{
 			return w.toString();
 		}
 	}
 	
 	void dot(Block b, PrintWriter out)
 	{
 		LabelWriter w = new LabelWriter(new StringWriter());
 		
 		// create the label
 		if (SHOW_CODE)
 			printssa(b,w);
 		else
 			w.print(b);
 		String attr = "label=\""+w+"\"";
 		
 		out.println(b+" ["+attr+"];");
 		
 		// CFG edges
 		for (Edge e: b.succ())
 			dot(e, out);
 		for (Edge e: b.xsucc)
 			dot(e, out);
 	}
 	
 	void dot_dfg(Block b, PrintWriter out)
 	{
 		LabelWriter w = new LabelWriter(new StringWriter());
 		
 		// create the label
 		w.print(b);
 		String attr = "label=\""+w+"\"; labeljust=l";
 		
 		// dfg edges
 		out.println("subgraph cluster"+b+" { "+attr+";");
 		Expr n = null;
 		Iterator<Expr> i = b.iterator();
 		if (i.hasNext())
 			n = i.next();
 		while (n != null)
 		{
 			Expr e = n;
 			w = new LabelWriter(new StringWriter());
 			printssa(e,w);
 			out.print("E"+e.id+" [label=\""+w+"\"];");
 			if (i.hasNext())
 			{
 				n = i.next();
 				out.print("E"+e.id+" -> E"+n.id+" [style=invisible,arrowhead=none,weight=4];");
 			}
 			else
 			{
 				n = null;
 			}
 		}
 		out.println("}");
 		for (Expr e: b)
 		{
 			for (Expr a: e.args)
 				out.print("E"+a.id+" -> E"+e.id+" [color=green];");
 			for (Expr a: e.locals)
 				out.print("E"+a.id+" -> E"+e.id+" [color=green];");
 			for (Expr a: e.scopes)
 				out.print("E"+a.id+" -> E"+e.id+" [color=grey,style=dashed];");
 			if (e.isPx())
 			{
 				for (Edge x: b.xsucc)
 				{
 					//out.print("E"+e.id+" -> E"+x.to.first().id+
 					//		" [weight=2,style=dashed,ltail=cluster"+x.from+",lhead=cluster"+x.to+"];");
 					out.print("E"+e.id+" -> E"+x.to.first().id+
 							" [weight=2,style=dashed,color=red];");
 				}
 			}
 		}
 		for (Edge s: b.succ())
 		{
 			Expr e = b.last();
 			int weight = s==b.last().succ[0] ? 4 : 2;
 			//out.print("E"+e.id+" -> E"+s.to.first().id+
 			//		" [weight="+weight+",ltail=cluster"+s.from+",lhead=cluster"+s.to+"];");
 			out.print("E"+e.id+" -> E"+s.to.first().id+
 					" [weight="+weight+"];");
 		}
 	}
 	
 	void dot(Edge e, PrintWriter out)
 	{
 		ArrayList<String> attrs = new ArrayList<String>();
 		if (isThrowEdge(e))
 		{
 			attrs.add("style=dashed");
 		}
 		else
 		{
 			// TODO smarter edge classification
 			if (e.isBackedge())
 				attrs.add("tailport=w,headport=w");
 
 			if (e.label == 0)
 				attrs.add("weight=2");
 			else
 				attrs.add("taillabel=\""+e.label+"\"");
 		}
 		out.println(e.from + " -> " + e.to + " "+attrs+";");
 	}
 
 	void verboseStatus(String msg)
 	{
 		if ( verbose_mode )
 			System.out.println(msg);
 	}
 
 	void verboseStatus(Object o)
 	{
 		if ( verbose_mode )
 			System.out.println(o.toString());
 	}
 	
 	class LocalVarState
 	{
 		/**
 		 *  Live-Out variables are variables that are
 		 *  used (without being redefined) in a successor
 		 *  block.  Note: the relationship is transitive.
 		 */
 		private BitSet liveout = new BitSet();
 		
 		/** 
 		 *  Variables killed in this block via explicit OP_kill insn. 
 		 */
 		private BitSet kills   = new BitSet();
 		
 		/**
 		 *  Variables defined in this block via setlocal.
 		 */
 		private BitSet def     = new BitSet();
 		
 		/**
 		 *  Upwardly-exposed variables, read in this block
 		 *  before any definition.  This means that their
 		 *  value comes from predecessor blocks. 
 		 */
 		private BitSet ue_vars = new BitSet();
 		
 		Map<Edge, BitSet> phi_inputs = new TreeMap<Edge, BitSet>();
 		
 		LocalVarState(Block b)
 		{
 			for ( Expr e: b.exprs)
 			{
 				switch(e.op)
 				{
 					case OP_getlocal0:
 					case OP_getlocal1:
 					case OP_getlocal2:
 					case OP_getlocal3:
 					{
 						uses( e.op - OP_getlocal0 );
 						break;
 					}
 					case OP_getlocal:
 					{
 						uses(e.imm[0]);
 						break;
 					}
 	
 					case OP_setlocal0:
 					case OP_setlocal1:
 					case OP_setlocal2:
 					case OP_setlocal3:
 					{
 						uses( e.op - OP_setlocal0 );
 						break;
 					}
 					
 					case OP_setlocal:
 					{
 						defines(e.imm[0]);
 						break;
 					}
 	
 					case OP_kill:
 					{
 						//  Just kills the variable
 						int varnum = e.imm[0];
 						kills.set(varnum);
 						break;
 					}
 					
 					case OP_phi:
 					{
 						defines(e.id);
 						
 						for (int i=e.args.length-1; i >= 0; i--)
 						{
 							addPhiInput(e.pred[i], e.args[i].id);
 						}
 						break;
 					}
 				}
 			}
 		}
 		
 		public boolean mergeLiveout(BitSet next_liveout) 
 		{
 			next_liveout.or(this.liveout);
 			boolean result = !this.liveout.equals(next_liveout);
 			
 			if ( result )
 			{
 				verboseStatus(this.toString() + " replacing " + this.liveout.toString() + " with " + next_liveout.toString());
 				this.liveout = next_liveout;
 			}
 			
 			return result;
 		}
 
 		boolean isPhiInput(Edge pred_edge, int var_index)
 		{
 			if ( null == phi_inputs.get(pred_edge))
 				return false;
 			else
 				return phi_inputs.get(pred_edge).get(var_index);
 		}
 		
 		BitSet getLivein()
 		{
 			BitSet result = (BitSet)this.liveout.clone();
 			result.andNot(this.def);
 			result.andNot(this.kills);
 			result.or(this.ue_vars);
 			return result;
 		}
 		
 		BitSet getLiveout()
 		{
 			return (BitSet)liveout.clone();
 		}
 		
 		private void addPhiInput(Edge pred_edge, int var_index)
 		{
 			if ( null == phi_inputs.get(pred_edge))
 			{
 				phi_inputs.put(pred_edge, new BitSet());
 			}
 			
 			phi_inputs.get(pred_edge).set(var_index);
 		}
 		
 		private void uses(int varnum)
 		{
 			if ( !def.get(varnum) )
 				ue_vars.set(varnum);
 		}
 		
 		private void defines(int varnum)
 		{
 			def.set(varnum);
 		}
 	}
 	class Reader
 	{
 		int pos;
 		byte[] abc;
 
 		Reader(int pos, byte[] abc)
 		{
 			this.pos = pos;
 			this.abc = abc;
 		}
 
 		Reader(Reader r)
 		{
 			this(r.pos, r.abc);
 		}
 
 		int readU8()
 		{
 			return 255 & abc[pos++];
 		}
 
 		int readU16()
 		{
 			return readU8() | readU8() << 8;
 		}
 
 		int readS24()
 		{
 			return readU16() | ((byte) readU8()) << 16;
 		}
 
 		int readU30()
 		{
 		    int result = readU8();
 		    if (0==(result & 0x00000080))
 		        return result;
 		    result = result & 0x0000007f | readU8()<<7;
 		    if (0==(result & 0x00004000))
 		        return result;
 		    result = result & 0x00003fff | readU8()<<14;
 		    if (0==(result & 0x00200000))
 		        return result;
 		    result = result & 0x001fffff | readU8()<<21;
 		    if (0==(result & 0x10000000))
 		        return result;
 		    return   result & 0x0fffffff | readU8()<<28;
 		}
 		
 		double readDouble()
 		{
 			return Double.longBitsToDouble(readU16() | ((long)readU16())<<16 |
 					((long)readU16())<<32 | ((long)readU16())<<48);
 		}
 	}
 	
 	class AbcWriter extends ByteArrayOutputStream
 	{
 		void rewind(int n)
 		{
 			super.count -= n;
 		}
 		void writeU16(int i)
 		{
 			write(i);
 			write(i>>8);
 		}
 		
 		void writeS24(int i)
 		{
 			writeU16(i);
 			write(i>>16);
 		}
 		
 		void write64(long i)
 		{
 			writeS24((int)i);
 			writeS24((int)(i>>24));
 			writeU16((int)(i>>48));
 		}
 		
 		void writeU30(int v)
 		{
 			if (v < 128 && v >= 0)
 			{
 				write(v);
 			}
 			else if (v < 16384 && v >= 0)
 			{
 				write(v & 0x7F | 0x80);
 				write(v >> 7);
 			}
 			else if (v < 2097152 && v >= 0)
 			{
 				write(v & 0x7F | 0x80);
 				write(v >> 7 | 0x80);
 				write(v >> 14);
 			}
 			else if (v < 268435456 && v >= 0)
 			{
 				write(v & 0x7F | 0x80);
 				write(v >> 7 | 0x80);
 				write(v >> 14 | 0x80);
 				write(v >> 21);
 			}
 			else
 			{
 				write(v & 0x7F | 0x80);
 				write(v >> 7 | 0x80);
 				write(v >> 14 | 0x80);
 				write(v >> 21 | 0x80);
 				write(v >> 28);
 			}
 		}
 	}
 	
 	static final int OPER=1;
 	static final int EFFECT=2; // has data side effects. 
 	static final int COERCE=4; // coerce or one of its shorthands
 	static final int PX=8; // potential exception
 	static final int SYNTH=0x10; //synthetic opcode, not to appear in a real abc 
 	static final int SCPVAL=0x20; // produces value on scope stack
 	static final int STKVAL=0x40; // produces value on stack
 	static final int LOCVAL=0x80; // produces result in a local variable
 
 	static int[] flagTable = new int[] {
 		LOCVAL|SYNTH,//"arg",
 	    EFFECT,//"bkpt",
 	    0,//"nop",
 	    PX,//"throw",
 	    STKVAL|EFFECT|PX,//"getsuper",
 	    EFFECT|PX,//"setsuper",
 	    EFFECT|PX,//"dxns",
 	    EFFECT|PX,//"dxnslate",
 	    LOCVAL|EFFECT,//"kill",
 	    EFFECT,//"label",
 	    SYNTH,//"phi",
 	    STKVAL|SYNTH,//"xarg",
 	    EFFECT|PX,//"ifnlt",
 	    EFFECT|PX,//"ifnle",
 	    EFFECT|PX,//"ifngt",
 	    EFFECT|PX,//"ifnge",
 	    EFFECT,//"jump",
 	    EFFECT,//"iftrue",
 	    EFFECT,//"iffalse",
 	    EFFECT|PX,//"ifeq",
 	    EFFECT|PX,//"ifne",
 	    EFFECT|PX,//"iflt",
 	    EFFECT|PX,//"ifle",
 	    EFFECT|PX,//"ifgt",
 	    EFFECT|PX,//"ifge",
 	    EFFECT,//"ifstricteq",
 	    EFFECT,//"ifstrictne",
 	    EFFECT|PX,//"lookupswitch",
 	    PX|SCPVAL,//"pushwith",
 	    EFFECT,//"popscope",
 	    STKVAL|PX,//"nextname",
 	    STKVAL|PX,//"hasnext",
 	    STKVAL,//"pushnull",
 	    STKVAL,//"pushundefined",
 	    0,//"OP_0x22",
 	    STKVAL|PX,//"nextvalue",
 	    STKVAL,//"pushbyte",
 	    STKVAL,//"pushshort",
 	    STKVAL,//"pushtrue",
 	    STKVAL,//"pushfalse",
 	    STKVAL,//"pushnan",
 	    EFFECT,//"pop",
 	    STKVAL|EFFECT,//"dup",
 	    EFFECT,//"swap",
 	    STKVAL,//"pushstring",
 	    STKVAL,//"pushint",
 	    STKVAL,//"pushuint",
 	    STKVAL,//"pushdouble",
 	    PX|SCPVAL,//"pushscope",
 	    STKVAL,//"pushnamespace",
 	    STKVAL|PX,//"hasnext2",
 	    LOCVAL|SYNTH,//"hasnext2_i",
 	    LOCVAL|SYNTH,//"hasnext2_o",
 	    0,//"OP_0x35",
 	    0,//"OP_0x36",
 	    0,//"OP_0x37",
 	    0,//"OP_0x38",
 	    0,//"OP_0x39",
 	    0,//"OP_0x3A",
 	    0,//"OP_0x3B",
 	    0,//"OP_0x3C",
 	    0,//"OP_0x3D",
 	    0,//"OP_0x3E",
 	    0,//"OP_0x3F",
 	    STKVAL,//"newfunction",
 	    STKVAL|EFFECT|PX,//"call",
 	    STKVAL|EFFECT|PX,//"construct",
 	    STKVAL|EFFECT|PX,//"callmethod",
 	    STKVAL|EFFECT|PX,//"callstatic",
 	    STKVAL|EFFECT|PX,//"callsuper",
 	    STKVAL|EFFECT|PX,//"callproperty",
 	    EFFECT|PX,//"returnvoid",
 	    EFFECT|PX,//"returnvalue",
 	    EFFECT|PX,//"constructsuper",
 	    STKVAL|EFFECT|PX,//"constructprop",
 	    0,//"0x4b
 	    STKVAL|EFFECT|PX,//"callproplex",
 	    0,//"0x4d
 	    EFFECT|PX,// "callsupervoid",
 	    EFFECT|PX,// "callpropvoid",
 	    0,//"OP_0x50",
 	    0,//"OP_0x51",
 	    0,//"OP_0x52",
 	    STKVAL|EFFECT|PX,//"applytype",
 	    0,//"OP_0x54",
 	    STKVAL,//"newobject",
 	    STKVAL,// "newarray",
 	    STKVAL,// "newactivation",
 	    STKVAL|EFFECT|PX,//  "newclass",
 	    STKVAL|PX,//  "getdescendants",
 	    STKVAL,//   "newcatch",
 	    0,//   "OP_0x5B",
 	    0,//   "OP_0x5C",
 	    /*EFFECT|*/STKVAL|PX,//   "findpropstrict",
 	    STKVAL|EFFECT|PX,//   "findproperty",
 	    STKVAL,//   "finddef",  add EFFECT|PX if we care about script loading side effects.
 	    STKVAL|EFFECT|PX,//   "getlex",
 	    EFFECT|PX,//   "setproperty",
 	    STKVAL|EFFECT,//   "getlocal",
 	    EFFECT,//   "setlocal",
 	    STKVAL,//   "getglobalscope",
 	    STKVAL,//   "getscopeobject",
 	    STKVAL|EFFECT|PX,//   "getproperty",
 	    0,//   "OP_0x67",
 	    EFFECT|PX,//   "initproperty",
 	    0,//   "OP_0x69",
 	    STKVAL|EFFECT|PX,//    "deleteproperty",
 	    0,//    "OP_0x6B",
 	    STKVAL|PX,//    "getslot"
 	    EFFECT|PX,//    "setslot",
 	    STKVAL|PX,//   "getglobalslot",
 	    EFFECT|PX,//   "setglobalslot",
 	    OPER|STKVAL|EFFECT|PX,//   "convert_s",
 	    OPER|STKVAL|EFFECT|PX,//   "esc_xelem",
 	    OPER|STKVAL|EFFECT|PX,//    "esc_xattr",
 	    OPER|STKVAL|EFFECT|PX,//    "convert_i",
 	    OPER|STKVAL|EFFECT|PX,//    "convert_u",
 	    OPER|STKVAL|EFFECT|PX,//    "convert_d",
 	    OPER|STKVAL,//    "convert_b",
 	    OPER|STKVAL|PX,//    "convert_o", aka null check
 	    PX,//    "checkfilter",
 	    0,//    "OP_0x79",
 	    0,//    "OP_0x7A",
 	    0,//    "OP_0x7B",
 	    0,//    "OP_0x7C",
 	    0,//    "OP_0x7D",
 	    0,//    "OP_0x7E",
 	    0,//    "OP_0x7F",
 	    OPER|STKVAL|EFFECT|PX|COERCE,//    "coerce",
 	    OPER|STKVAL|COERCE,//    "coerce_b",
 	    OPER|STKVAL|COERCE,//    "coerce_a",
 	    OPER|STKVAL|EFFECT|PX|COERCE,//    "coerce_i",
 	    OPER|STKVAL|EFFECT|PX|COERCE,//    "coerce_d",
 	    OPER|STKVAL|EFFECT|PX|COERCE,//    "coerce_s",
 	    OPER|STKVAL|EFFECT|PX,//    "astype",
 	    OPER|STKVAL|EFFECT|PX,//    "astypelate",
 	    OPER|STKVAL|EFFECT|PX|COERCE,//    "coerce_u",
 	    OPER|STKVAL|PX|COERCE,//    "coerce_o",
 	    0,//    "OP_0x8A",
 	    0,//    "OP_0x8B",
 	    0,//    "OP_0x8C",
 	    0,//    "OP_0x8D",
 	    0,//    "OP_0x8E",
 	    0,//    "OP_0x8F",
 	    OPER|STKVAL|EFFECT|PX,//    "negate",
 	    OPER|STKVAL|EFFECT|PX,//  "increment",
 	    OPER|LOCVAL|EFFECT|PX,//  "inclocal",
 	    OPER|STKVAL|EFFECT|PX,//  "decrement",
 	    OPER|LOCVAL|EFFECT|PX,//   "declocal",
 	    OPER|STKVAL,//   "typeof",
 	    OPER|STKVAL,//   "not",
 	    OPER|STKVAL|EFFECT|PX,//   "bitnot",
 	    0,//    "OP_0x98",
 	    0,//    "OP_0x99",
 	    0,//    "OP_0x9A",
 	    0,//    "OP_0x9B",
 	    0,//    "OP_0x9C",
 	    0,//    "OP_0x9D",
 	    0,//    "OP_0x9E",
 	    0,//    "OP_0x9F",
 	    OPER|STKVAL|EFFECT|PX,//    "add",
 	    OPER|STKVAL|EFFECT|PX,//    "subtract",
 	    OPER|STKVAL|EFFECT|PX,//    "multiply",
 	    OPER|STKVAL|EFFECT|PX,//    "divide",
 	    OPER|STKVAL|EFFECT|PX,//   "modulo",
 	    OPER|STKVAL|EFFECT|PX,//   "lshift",
 	    OPER|STKVAL|EFFECT|PX,//   "rshift",
 	    OPER|STKVAL|EFFECT|PX,//   "urshift",
 	    OPER|STKVAL|EFFECT|PX,//   "bitand",
 	    OPER|STKVAL|EFFECT|PX,//    "bitor",
 	    OPER|STKVAL|EFFECT|PX,//    "bitxor",
 	    OPER|STKVAL|EFFECT|PX,//    "equals",
 	    OPER|STKVAL,//    "strictequals",
 	    OPER|STKVAL|EFFECT|PX,//    "lessthan",
 	    OPER|STKVAL|EFFECT|PX,//    "lessequals",
 	    OPER|STKVAL|EFFECT|PX,//    "greaterthan",
 	    OPER|STKVAL|EFFECT|PX,//    "greaterequals",
 	    OPER|STKVAL|EFFECT|PX,//   "instanceof",
 	    OPER|STKVAL|EFFECT|PX,//   "istype", (pure if type is known)
 	    OPER|STKVAL|PX,//   "istypelate", (pure if rhs is Class)
 	    OPER|STKVAL|PX,//    "in",
 	    0,//    "OP_0xB5",
 	    0,//    "OP_0xB6",
 	    0,//    "OP_0xB7",
 	    0,//    "OP_0xB8",
 	    0,//    "OP_0xB9",
 	    0,//    "OP_0xBA",
 	    0,//    "OP_0xBB",
 	    0,//    "OP_0xBC",
 	    0,//    "OP_0xBD",
 	    0,//    "OP_0xBE",
 	    0,//    "OP_0xBF",
 	    OPER|STKVAL|EFFECT|PX,//    "increment_i",
 	    OPER|STKVAL|EFFECT|PX,//    "decrement_i",
 	    OPER|LOCVAL|EFFECT|PX,//    "inclocal_i",
 	    OPER|LOCVAL|EFFECT|PX,//    "declocal_i",
 	    OPER|STKVAL|EFFECT|PX,//    "negate_i",
 	    OPER|STKVAL|EFFECT|PX,//    "add_i",
 	    OPER|STKVAL|EFFECT|PX,//    "subtract_i",
 	    OPER|STKVAL|EFFECT|PX,//    "multiply_i",
 	    0,//    "OP_0xC8",
 	    0,//    "OP_0xC9",
 	    0,//    "OP_0xCA",
 	    0,//    "OP_0xCB",
 	    0,//    "OP_0xCC",
 	    0,//    "OP_0xCD",
 	    0,//    "OP_0xCE",
 	    0,//    "OP_0xCF",
 	    STKVAL|EFFECT,//    "getlocal0",
 	    STKVAL|EFFECT,//    "getlocal1",
 	    STKVAL|EFFECT,//    "getlocal2",
 	    STKVAL|EFFECT,//    "getlocal3",
 	    EFFECT,//    "setlocal0",
 	    EFFECT,//    "setlocal1",
 	    EFFECT,//    "setlocal2",
 	    EFFECT,//    "setlocal3",
 	    0,//    "OP_0xD8",
 	    0,//    "OP_0xD9",
 	    0,//   "OP_0xDA",
 	    0,//   "OP_0xDB",
 	    0,//   "OP_0xDC",
 	    0,//   "OP_0xDD",
 	    0,//   "OP_0xDE",
 	    0,//  "OP_0xDF",
 	    0,//   "OP_0xE0",
 	    0,//  "OP_0xE1",
 	    0,//   "OP_0xE2",
 	    0,//  "OP_0xE3",
 	    0,//    "OP_0xE4",
 	    0,//    "OP_0xE5",
 	    0,//    "OP_0xE6",
 	    0,//    "OP_0xE7",
 	    0,//    "OP_0xE8",
 	    0,//    "OP_0xE9",
 	    0,//    "OP_0xEA",
 	    0,//    "OP_0xEB",
 	    0,//    "OP_0xEC",
 	    0,//    "OP_0xED",
 	    SYNTH,//    "abs_jump",
 	    EFFECT,//    "debug",
 	    EFFECT,//    "debugline",
 	    EFFECT,//    "debugfile",
 	    EFFECT,//    "bkptline",
 	    SYNTH,//    "timestamp",
 	    0,//    "OP_0xF4",
 	    SYNTH,//    "verifypass",
 	    SYNTH,//    "alloc",
 	    SYNTH,//    "mark",
 	    SYNTH,//    "wb",
 	    SYNTH,//    "prologue",
 	    SYNTH,//    "sendenter",
 	    SYNTH,//   "doubletoatom",
 	    SYNTH,//   "sweep",
 	    SYNTH,//   "codegenop",
 	    SYNTH,//   "verifyop",
 	    SYNTH,//   "decode"
 	};
 	static String[] opNames =
 	{
     "arg",		// synthetic
     "bkpt",
     "nop",
     "throw",
     "getsuper",
     "setsuper",
     "dxns",
     "dxnslate",
     "kill",
     "label",
     "phi",//"\u0278",//"\u03d5",//"\u03c6",//"&Phi;",		// synthetic
     //"\u0278",//"\u03d5",//"\u03c6",//"&Phi;",		// synthetic
     "xarg",		// synthetic
     "ifnlt",
     "ifnle",
     "ifngt",
     "ifnge",
     "jump",
     "iftrue",
     "iffalse",
     "ifeq",
     "ifne",
     "iflt",
     "ifle",
     "ifgt",
     "ifge",
     "ifstricteq",
     "ifstrictne",
     "lookupswitch",
     "pushwith",
     "popscope",
     "nextname",
     "hasnext",
     "pushnull",
     "pushundefined",
     "OP_0x22",
     "nextvalue",
     "pushbyte",
     "pushshort",
     "pushtrue",
     "pushfalse",
     "pushnan",
     "pop",
     "dup",
     "swap",
     "pushstring",
     "pushint",
     "pushuint",
     "pushdouble",
     "pushscope",
     "pushnamespace",
     "hasnext2",
     "hasnext2_i",
     "hasnext2_o",
     "OP_0x35",
     "OP_0x36",
     "OP_0x37",
     "OP_0x38",
     "OP_0x39",
     "OP_0x3A",
     "OP_0x3B",
     "OP_0x3C",
     "OP_0x3D",
     "OP_0x3E",
     "OP_0x3F",
     "newfunction",
     "call",
     "construct",
     "callmethod",
     "callstatic",
     "callsuper",
     "callproperty",
     "returnvoid",
     "returnvalue",
     "constructsuper",
     "constructprop",
     "OP_0x4B",
     "callproplex",
     "OP_0x4D",
     "callsupervoid",
     "callpropvoid",
     "OP_0x50",
     "OP_0x51",
     "OP_0x52",
     "applytype",
     "OP_0x54",
     "newobject",
     "newarray",
     "newactivation",
     "newclass",
     "getdescendants",
     "newcatch",
     "OP_0x5B",
     "OP_0x5C",
     "findpropstrict",
     "findproperty",
     "finddef",
     "getlex",
     "setproperty",
     "getlocal",
     "setlocal",
     "getglobalscope",
     "getscopeobject",
     "getproperty",
     "OP_0x67",
     "initproperty",
     "OP_0x69",
     "deleteproperty",
     "OP_0x6B",
     "getslot",
     "setslot",
     "getglobalslot",
     "setglobalslot",
     "convert_s",
     "esc_xelem",
     "esc_xattr",
     "convert_i",
     "convert_u",
     "convert_d",
     "convert_b",
     "convert_o",
     "checkfilter",
     "OP_0x79",
     "OP_0x7A",
     "OP_0x7B",
     "OP_0x7C",
     "OP_0x7D",
     "OP_0x7E",
     "OP_0x7F",
     "coerce",
     "coerce_b",
     "coerce_a",
     "coerce_i",
     "coerce_d",
     "coerce_s",
     "astype",
     "astypelate",
     "coerce_u",
     "coerce_o",
     "OP_0x8A",
     "OP_0x8B",
     "OP_0x8C",
     "OP_0x8D",
     "OP_0x8E",
     "OP_0x8F",
     "negate",
     "increment",
     "inclocal",
     "decrement",
     "declocal",
     "typeof",
     "not",
     "bitnot",
     "OP_0x98",
     "OP_0x99",
     "OP_0x9A",
     "OP_0x9B",
     "OP_0x9C",
     "OP_0x9D",
     "OP_0x9E",
     "OP_0x9F",
     "add",
     "subtract",
     "multiply",
     "divide",
     "modulo",
     "lshift",
     "rshift",
     "urshift",
     "bitand",
     "bitor",
     "bitxor",
     "equals",
     "strictequals",
     "lessthan",
     "lessequals",
     "greaterthan",
     "greaterequals",
     "instanceof",
     "istype",
     "istypelate",
     "in",
     "OP_0xB5",
     "OP_0xB6",
     "OP_0xB7",
     "OP_0xB8",
     "OP_0xB9",
     "OP_0xBA",
     "OP_0xBB",
     "OP_0xBC",
     "OP_0xBD",
     "OP_0xBE",
     "OP_0xBF",
     "increment_i",
     "decrement_i",
     "inclocal_i",
     "declocal_i",
     "negate_i",
     "add_i",
     "subtract_i",
     "multiply_i",
     "OP_0xC8",
     "OP_0xC9",
     "OP_0xCA",
     "OP_0xCB",
     "OP_0xCC",
     "OP_0xCD",
     "OP_0xCE",
     "OP_0xCF",
     "getlocal0",
     "getlocal1",
     "getlocal2",
     "getlocal3",
     "setlocal0",
     "setlocal1",
     "setlocal2",
     "setlocal3",
     "OP_0xD8",
     "OP_0xD9",
     "OP_0xDA",
     "OP_0xDB",
     "OP_0xDC",
     "OP_0xDD",
     "OP_0xDE",
     "OP_0xDF",
     "OP_0xE0",
     "OP_0xE1",
     "OP_0xE2",
     "OP_0xE3",
     "OP_0xE4",
     "OP_0xE5",
     "OP_0xE6",
     "OP_0xE7",
     "OP_0xE8",
     "OP_0xE9",
     "OP_0xEA",
     "OP_0xEB",
     "OP_0xEC",
     "OP_0xED",
     "OP_0xEE",
     "debug",
     "debugline",
     "debugfile",
     "bkptline",
     "timestamp",
     "OP_0xF4",
     "OP_0xF5",
     "OP_0xF6",
     "OP_0xF7",
     "OP_0xF8",
     "OP_0xF9",
     "OP_0xFA",
     "OP_0xFB",
     "OP_0xFC",
     "OP_0xFD",
     "OP_0xFE",
     "OP_0xFF"
 	};
 }
 
 /**
  * internal Opcodes (used by optimizer not by AVM)
  */
 interface ActionBlockConstants
 {
     int OP_arg = 0x00; // argument provided by the vm in a pre-assigned local var.
     int OP_phi = 0x0a; // compiler only
     int OP_xarg = 0x0b; // represents the exception object passed to catch
     int OP_hasnext2_i = 0x33; // hasnext2 index
     int OP_hasnext2_o = 0x34; // hasnext2 object
 }
