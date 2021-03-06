 package org.badiff;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel.MapMode;
 
 import org.badiff.imp.FileDiff;
 import org.badiff.io.DefaultSerialization;
 import org.badiff.io.Serialization;
 import org.badiff.q.BufferChunkingOpQueue;
 import org.badiff.q.OneWayOpQueue;
import org.badiff.q.OpQueue;
 import org.badiff.q.UndoOpQueue;
 import org.badiff.util.Diffs;
 
 /**
  * Utilities for dealing with {@link Diff}s as {@link File}
  * @author robin
  *
  */
 public class FileDiffs {
 
 	/**
 	 * The {@link Serialization} for persistence
 	 */
 	protected Serialization serial;
 	
 	/**
 	 * Create a new {@link FileDiffs} utilities instance
 	 */
 	public FileDiffs() {
 		this(DefaultSerialization.getInstance());
 	}
 	
 	/**
 	 * Create a new {@link FileDiffs} utilities instance with a specified {@link Serialization}
 	 * @param serial
 	 */
 	public FileDiffs(Serialization serial) {
 		this.serial = serial;
 	}
 
 	/**
 	 * Compute and return a diff between {@code orig} and {@code target}
 	 * @param orig
 	 * @param target
 	 * @return
 	 */
 	public FileDiff diff(File orig, File target) throws IOException {
 		FileDiff fd = new FileDiff(File.createTempFile(orig.getName(), ".diff"));
 		InputStream oin = new FileInputStream(orig);
 		try {
 			InputStream tin = new FileInputStream(target);
 			try {
 				fd.store(Diffs.improved(Diffs.queue(oin, tin)));
 			} finally {
 				tin.close();
 			}
 		} finally {
 			oin.close();
 		}
 		return fd;
 	}
 	
 	/**
 	 * Compute and return a diff between {@code orig} and {@code target} using
 	 * memory-mapped files
 	 * @param orig
 	 * @param target
 	 * @return
 	 * @throws IOException
 	 */
 	public FileDiff mdiff(File orig, File target) throws IOException {
 		FileDiff fd = new FileDiff(File.createTempFile(orig.getName(), ".diff"));
 		FileInputStream oin = new FileInputStream(orig);
 		try {
 			FileInputStream tin = new FileInputStream(target);
 			try {
 				MappedByteBuffer obuf = oin.getChannel().map(MapMode.READ_ONLY, 0, orig.length());
 				MappedByteBuffer tbuf = tin.getChannel().map(MapMode.READ_ONLY, 0, target.length());
 				fd.store(Diffs.improved(new BufferChunkingOpQueue(obuf, tbuf)));
 			} finally {
 				tin.close();
 			}
 		} finally {
 			oin.close();
 		}
 		return fd;
 	}
 	
 	/**
 	 * Apply {@code diff} to {@code orig} and return the result
 	 * @param orig
 	 * @param diff
 	 * @return
 	 */
 	public File apply(File orig, Diff diff) throws IOException {
 		File target = File.createTempFile(orig.getName(), ".target");
 		Diffs.apply(diff, orig, target);
 		return target;
 	}
 	
 	/**
 	 * Compute and return a one-way (unidirectional) diff from {@code orig} to {@code target}
 	 * @param orig
 	 * @param target
 	 * @return
 	 */
 	public FileDiff udiff(File orig, File target) throws IOException {
 		FileDiff fd = new FileDiff(File.createTempFile(orig.getName(), ".udiff"));
 		InputStream oin = new FileInputStream(orig);
 		try {
 			InputStream tin = new FileInputStream(target);
 			try {
 				fd.store(new OneWayOpQueue(Diffs.improved(Diffs.queue(oin, tin))));
 			} finally {
 				tin.close();
 			}
 		} finally {
 			oin.close();
 		}
 		return fd;
 	}
 	
 	/**
 	 * Apply the inverse of {@code diff} to {@code target} and return the result
 	 * @param target
 	 * @param diff
 	 * @return
 	 */
 	public File undo(File target, Diff diff) throws IOException {
 		File orig = File.createTempFile(target.getName(), ".orig");
 		Diffs.apply(new UndoOpQueue(diff.queue()), target, orig);
 		return orig;
 	}
 	
 	/**
 	 * Compute and return a one-way (unidirectional) diff given any diff
 	 * @param diff
 	 * @return
 	 */
 	public FileDiff udiff(Diff diff) throws IOException {
 		FileDiff ud = new FileDiff(File.createTempFile("udiff", ".udiff"));
 		ud.store(new OneWayOpQueue(diff.queue()));
 		return ud;
 	}
 	
 	/**
 	 * Compute and return an "undo" diff from a two-way diff
 	 * @param diff
 	 * @return
 	 */
 	public FileDiff undo(Diff diff) throws IOException {
 		FileDiff ud = new FileDiff(File.createTempFile("undo", ".undo"));
 		ud.store(new UndoOpQueue(diff.queue()));
 		return ud;
 	}
 }
