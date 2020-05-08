 package org.magnolialang.terms;
 
 import static org.magnolialang.terms.TermFactory.ts;
 import static org.magnolialang.terms.TermFactory.vf;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.imp.pdb.facts.*;
 import org.eclipse.imp.pdb.facts.io.PBFReader;
 import org.magnolialang.eclipse.MagnoliaPlugin;
 import org.magnolialang.errors.ImplementationError;
 
 public class SkinTable {
 	private final String tableName;
 	private IMap table;
 
 	public SkinTable(final String tableName) {
 		this.tableName = tableName;
 		loadTable();
 	}
 
 	private void loadTable() {
 		final PBFReader reader = new PBFReader();
 		InputStream stream = null;
		final IPath path = new Path("lang/" + tableName);
 		try {
			stream = FileLocator.openStream(MagnoliaPlugin.MAGNOLIA_BUNDLE,
					path, false);
 			table = (IMap) reader.read(vf, ts, null, stream);
 		}
 		catch(final IOException e) {
 			throw new ImplementationError("Unable to read pretty print table "
					+ tableName + " from lang/" + path, e);
 		}
 		finally {
 			if(stream != null)
 				try {
 					stream.close();
 				}
 				catch(final IOException e) {
 				}
 		}
 	}
 
 	public IList getConcrete(final String consname) {
 		final ITuple entry = getEntry(consname);
 		if(entry != null)
 			return (IList) entry.get(0);
 		else
 			return null;
 	}
 
 	public String getSyntax(final String consname) {
 		final ITuple entry = getEntry(consname);
 		if(entry != null)
 			return ((IString) entry.get(1)).getValue();
 		else
 			return null;
 	}
 
 	public String getSort(final String consname) {
 		final ITuple entry = getEntry(consname);
 		if(entry != null)
 			return ((IString) entry.get(2)).getValue();
 		else
 			return null;
 	}
 
 	public ITuple getEntry(final String consname) {
 		final IValue entry = table.get(vf.string(consname));
 		if(entry != null && entry instanceof ITuple)
 			return (ITuple) entry;
 		else
 			return null;
 
 	}
 }
