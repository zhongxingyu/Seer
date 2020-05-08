 package no.runsafe.framework.minecraft.networking;
 
 import net.minecraft.server.v1_6_R3.DedicatedServerConnection;
 import net.minecraft.server.v1_6_R3.MinecraftServer;
 import no.runsafe.framework.api.IConsole;
import no.runsafe.framework.internal.networking.RunsafeServerConnectionThread;
 
 import java.io.IOException;
 import java.net.InetAddress;
 
 public class RunsafeServerConnection extends DedicatedServerConnection
 {
	private final RunsafeServerConnectionThread thread;
 
 	public RunsafeServerConnection(MinecraftServer server, InetAddress address, int i, IConsole output) throws IOException
 	{
 		super(server, address, i); // We have to do this..
 		super.a(); // Kill the thread DSC just spawned.
 
 		this.output = output;
 		this.output.logInformation("RunsafeServerConnection successfully loaded.");
 
		thread = new RunsafeServerConnectionThread(this, address, i);
 		thread.start();
 	}
 
 	@Override
 	public void a()
 	{
 		a = false;
 		thread.b();
 		thread.interrupt();
 	}
 
 	@Override
 	public void b()
 	{
 		thread.a();
 		super.b(); // This might cause errors.
 	}
 
 	@Override
 	public void a(InetAddress inetAddress)
 	{
 		thread.a(inetAddress);
 	}
 
 	private final IConsole output;
 }
