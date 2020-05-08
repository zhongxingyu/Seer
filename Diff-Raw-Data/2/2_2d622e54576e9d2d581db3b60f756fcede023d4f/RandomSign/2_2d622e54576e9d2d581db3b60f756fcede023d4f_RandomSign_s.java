 package graindcafe.tribu.Signs;
 
 import graindcafe.tribu.Tribu;
 import graindcafe.tribu.Level.TribuLevel;
 
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.block.Sign;
 import org.bukkit.event.Event;
 
 public class RandomSign extends ShopSign {
 
 	List<String> packages;
 	Random rand;
 	String pckName;
 
 	public RandomSign(Tribu plugin) {
 		super(plugin);
 	}
 
 	/**
 	 * 
 	 * @param plugin
 	 *            Tribu
 	 * @param pos
 	 *            Location of the sign
 	 * @param item
 	 *            Material to be sold
 	 * @param cost
 	 */
 	public RandomSign(final Tribu plugin, final Location pos,
 			List<String> packages, final int cost) {
 		super(plugin, pos, cost);
 		this.packages = packages;
 		rand = plugin.getRandom();
 	}
 
 	/**
 	 * @param plugin
 	 *            Tribu
 	 * @param pos
 	 *            Location of the sign
 	 * @param signLines
 	 *            Lines of this sign
 	 */
 	public RandomSign(final Tribu plugin, final Location pos,
 			final String[] signLines) {
 		this(plugin, pos, getPackages(signLines, plugin.getLevel()), TribuSign
 				.parseInt(signLines[3]));
 	}
 
 	protected static List<String> getPackages(final String[] signLines,
 			final TribuLevel level) {
 		if (signLines == null || level == null)
 			return null;
 		List<String> i;
 		/* Try to get a package */
 		i = level.getMysteryPackage((signLines[1] + "_" + signLines[2]));
 		if (i == null || i.isEmpty())
 			i = level.getMysteryPackage(signLines[1]);
 		if (i == null || i.isEmpty())
 			i = level.getMysteryPackage(signLines[2]);
 		return i;
 	}
 
 	@Override
 	protected String[] getSpecificLines() {
 		final String[] lines = new String[4];
 		final int idx = pckName.lastIndexOf('_');
 		lines[0] = "";
 
 		if (idx < 0) {
 			lines[1] = pckName;
 			lines[2] = "";
 		} else {
 			lines[1] = pckName.substring(0, idx);
 			lines[2] = pckName.substring(idx + 1);
 		}
 		lines[3] = String.valueOf(cost);
 		return lines;
 	}
 
 	@Override
 	public void init() {
 		if (pos.getBlock().getState() instanceof Sign)
 			packages = getPackages(
 					((Sign) pos.getBlock().getState()).getLines(),
 					plugin.getLevel());
 		else
 			plugin.LogWarning("Missing sign !");
 	}
 
 	@Override
 	public boolean isUsedEvent(Event e) {
		return super.isUsedEvent(e);
 	}
 
 	@Override
 	public void raiseEvent(Event e) {
 		String name = packages.get(rand.nextInt(packages.size()));
 		if (name != null)
 			this.items = plugin.getLevel().getPackage(name);
 		super.raiseEvent(e);
 	}
 }
