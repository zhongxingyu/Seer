 package com.bluesky.visualprogramming.core.nativeproc.string;
 
 import com.bluesky.visualprogramming.core.NativeProcedure;
 import com.bluesky.visualprogramming.core.ObjectScope;
 import com.bluesky.visualprogramming.core.ObjectType;
 import com.bluesky.visualprogramming.core._Object;
 import com.bluesky.visualprogramming.core.nativeproc.BaseNativeProcedure;
 import com.bluesky.visualprogramming.core.value.StringValue;
 import com.bluesky.visualprogramming.vm.ProcedureExecutionContext;
 import com.bluesky.visualprogramming.vm.VirtualMachine;
 
 public class ReplaceAll extends BaseNativeProcedure implements NativeProcedure {
 
 	@Override
 	protected _Object execute(VirtualMachine virtualMachine, _Object self,
 			ProcedureExecutionContext ctx) {
 		StringValue oldSV = (StringValue) ctx.get("old");
 		StringValue newSV = (StringValue) ctx.get("new");
 
 		StringValue selfStr = (StringValue) self;
 
		StringValue result = (StringValue) virtualMachine.getObjectRepository()
				.createObject(ObjectType.STRING, ObjectScope.ExecutionContext);
 
		String newStr = selfStr.getValue().replaceAll(oldSV.getValue(), newSV.getValue());
 
		result.setValue(newStr);
 
		return result;
 	}
 }
