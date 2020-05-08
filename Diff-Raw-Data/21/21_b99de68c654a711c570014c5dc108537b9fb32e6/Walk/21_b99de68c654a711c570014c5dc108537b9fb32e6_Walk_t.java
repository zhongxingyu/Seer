 package com.battleguard.scripts.f2prunecrafter.nodes.impl;
 
 import org.powerbot.script.methods.MethodContext;
 import org.powerbot.script.methods.MethodProvider;
 import org.powerbot.script.util.Timer;
 import org.powerbot.script.wrappers.TilePath;
 
 import com.battleguard.scripts.f2prunecrafter.data.Master;
 import com.battleguard.scripts.f2prunecrafter.nodes.Node;
 
 public class Walk extends MethodProvider implements Node {
 
 	private final TilePath path;
 	private final Timer nextStep = new Timer(1000);
 	
 	private Walk(final TilePath path, MethodContext ctx) {
 		super(ctx);
 		this.path = path;
 	}
 	
 	public static Walk bankPathInstance(Master master, MethodContext ctx) {
		return new Walk(master.path().toBank(ctx), ctx);
 	}
 	
 	public static Walk alterPathInstance(Master master, MethodContext ctx) {
		return new Walk(master.path().toAlter(ctx), ctx);
 	}
 	
 	@Override
 	public boolean activate() {
 		return !nextStep.isRunning();
 	}
 
 	@Override
 	public void execute() {		
 		path.traverse();
 		nextStep.reset();
 	}
 
 }
