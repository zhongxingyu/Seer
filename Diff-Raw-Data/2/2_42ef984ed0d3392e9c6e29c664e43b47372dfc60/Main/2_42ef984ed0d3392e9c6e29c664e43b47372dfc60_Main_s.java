 /*-
  * $Id$
  */
 package com.example;
 
 import static java.awt.BorderLayout.CENTER;
 import static java.lang.Class.forName;
 import static java.lang.System.getProperty;
 import static java.util.Arrays.asList;
 import static java.util.Collections.list;
 import static javax.swing.JFrame.EXIT_ON_CLOSE;
 import static javax.swing.JFrame.isDefaultLookAndFeelDecorated;
 import static javax.swing.JFrame.setDefaultLookAndFeelDecorated;
 import static javax.swing.JRootPane.FRAME;
 import static javax.swing.SwingUtilities.invokeLater;
 import static javax.swing.SwingUtilities.updateComponentTreeUI;
 import static javax.swing.UIManager.getLookAndFeel;
 import static javax.swing.UIManager.setLookAndFeel;
 import static javax.swing.plaf.metal.MetalLookAndFeel.getCurrentTheme;
 import static javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.jar.JarEntry;
 import java.util.jar.JarInputStream;
 
 import javax.swing.AbstractButton;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.LookAndFeel;
 import javax.swing.plaf.metal.MetalLookAndFeel;
 import javax.swing.plaf.metal.MetalTheme;
 import javax.swing.plaf.multi.MultiLookAndFeel;
 import javax.swing.plaf.synth.SynthLookAndFeel;
 
 import org.eclipse.jdt.annotation.Nullable;
 
 /**
  * @author Andrew ``Bass'' Shcheglov (mailto:andrewbass@gmail.com)
  */
 abstract class Main {
 	/**
 	 * @see "<em>The Java Virtual Machine Specification</em>, table 4.1"
 	 */
 	private static short ACC_PUBLIC = 0x0001;
 
 	/**
 	 * @see "<em>The Java Virtual Machine Specification</em>, table 4.1"
 	 */
 	private static short ACC_ABSTRACT = 0x0400;
 
 	private Main() {
 		assert false;
 	}
 
 	/**
 	 * @param path
 	 */
 	private static String[] split(@Nullable final String path) {
 		if (path == null || path.length() == 0) {
 			return new String[0];
 		}
 		final String pathSeparator = getProperty("path.separator");
 		final String[] entries = path.split("\\Q" + pathSeparator + "\\E");
 		return entries == null ? new String[0] : entries;
 	}
 
 	/**
 	 * @param baseClass
 	 * @param skipInnerClasses
 	 * @param skipAnonymousClasses
 	 * @param skipNonPublic
 	 * @param skipAbstract
 	 * @param skipDeprecated
 	 */
 	private static <T> SortedSet<Class<? extends T>> listDescendants(final Class<T> baseClass,
 			final boolean skipInnerClasses,
 			final boolean skipAnonymousClasses,
 			final boolean skipNonPublic,
 			final boolean skipAbstract,
 			final boolean skipDeprecated) {
 		final String javaClassPath = getProperty("java.class.path");
 		final String sunBootClassPath = getProperty("sun.boot.class.path");
 		final List<String> pathEntries = new ArrayList<>();
 		pathEntries.addAll(asList(split(javaClassPath)));
 		pathEntries.addAll(asList(split(sunBootClassPath)));
 		final SortedSet<Class<? extends T>> classes = new TreeSet<>(new Comparator<Class<? extends T>>() {
 			/**
 			 * @see Comparator#compare
 			 */
 			@Override
 			public int compare(final @Nullable Class<? extends T> class0, final @Nullable Class<? extends T> class1) {
 				if (class0 == null || class1 == null) {
 					throw new IllegalArgumentException();
 				}
 				return class0.getName().compareTo(class1.getName());
 			}
 		});
 		for (final String path : pathEntries) {
 			final File file = new File(path);
 			if (!file.exists() || file.isDirectory() || !file.isFile()) {
 				continue;
 			}
 			try (final InputStream in = new FileInputStream(file);
 					final JarInputStream jis = new JarInputStream(in)) {
 				JarEntry entry;
 				while ((entry = jis.getNextJarEntry()) != null) {
 					if (entry.isDirectory()) {
 						/*
 						 * Skip directories.
 						 */
 						continue;
 					}
 					final String entryName = entry.getName();
 					final int indexOfDotClass = entryName.indexOf(".class");
 					if (indexOfDotClass == -1) {
 						/*
 						 * Skip resources.
 						 */
 						continue;
 					}
 					final String className = entryName.substring(0, indexOfDotClass).replace('/', '.');
 					if (skipAnonymousClasses && className.matches(".*[^\\$](\\$\\d+)+$")) {
 						continue;
 					}
 
 					if (skipInnerClasses && className.indexOf('$') != -1) {
 						continue;
 					}
 
 					try {
 						final Class<?> clazz = forName(className);
 						if (!baseClass.isAssignableFrom(clazz)) {
 							continue;
 						}
 
 						final int modifiers = clazz.getModifiers();
 						if (skipNonPublic && (modifiers & ACC_PUBLIC) == 0) {
 							continue;
 						}
 
 						if (skipAbstract && (clazz.getModifiers() & ACC_ABSTRACT) != 0) {
 							continue;
 						}
 
 						if (skipDeprecated && clazz.isAnnotationPresent(Deprecated.class)) {
 							continue;
 						}
 
 						@SuppressWarnings("unchecked")
 						final Class<? extends T> class2 = (Class<? extends T>) clazz;
 						classes.add(class2);
 					} catch (final ClassNotFoundException | UnsatisfiedLinkError | ExceptionInInitializerError | NoClassDefFoundError e) {
 						// ignore
 					} catch (final OutOfMemoryError oome) {
 						throw oome;
 					} catch (final Throwable t) {
						t.printStackTrace();
 					}
 				}
 			} catch (final IOException ioe) {
 				// ignore
 			}
 		}
 		return classes;
 	}
 
 	/**
 	 * @param lookAndFeel
 	 * @param themeMenu
 	 * @param c
 	 */
 	private static JRadioButtonMenuItem fromLookAndFeel(final @Nullable LookAndFeel lookAndFeel,
 			final JMenu themeMenu,
 			final JFrame frame) {
 		if (lookAndFeel == null) {
 			throw new IllegalArgumentException();
 		}
 
 		final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem();
 		menuItem.setText(lookAndFeel.getName());
 		menuItem.setToolTipText(lookAndFeel.getClass().getName());
 		menuItem.setEnabled(lookAndFeel.isSupportedLookAndFeel());
 		menuItem.setSelected(getLookAndFeel().getClass() == lookAndFeel.getClass());
 		menuItem.addActionListener(new ActionListener() {
 			/**
 			 * @see ActionListener#actionPerformed(ActionEvent)
 			 */
 			@Override
 			public void actionPerformed(final @Nullable ActionEvent e) {
 				try {
 					setLookAndFeel(lookAndFeel);
 					themeMenu.setEnabled(lookAndFeel instanceof MetalLookAndFeel);
 					updateComponentTreeUI(frame);
 
 					if (isDefaultLookAndFeelDecorated()) {
 						frame.dispose();
 						frame.setUndecorated(lookAndFeel instanceof MetalLookAndFeel);
 						frame.getRootPane().setWindowDecorationStyle(FRAME);
 						frame.setVisible(true);
 					}
 				} catch (final Exception e1) {
 					menuItem.setEnabled(false);
 				}
 			}
 		});
 		return menuItem;
 	}
 
 	/**
 	 * @param metalTheme
 	 * @param c
 	 */
 	private static JRadioButtonMenuItem fromMetalTheme(final @Nullable MetalTheme metalTheme, final Component c) {
 		if (metalTheme == null) {
 			throw new IllegalArgumentException();
 		}
 
 		final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem();
 		menuItem.setText(metalTheme.getName());
 		final String className = metalTheme.getClass().getName();
 		menuItem.setToolTipText(className);
 		menuItem.setName(className);
 		menuItem.addActionListener(new ActionListener() {
 			/**
 			 * @see ActionListener#actionPerformed(ActionEvent)
 			 */
 			@Override
 			public void actionPerformed(final @Nullable ActionEvent e) {
 				try {
 					setCurrentTheme(metalTheme);
 					setLookAndFeel(getLookAndFeel());
 
 					updateComponentTreeUI(c);
 				} catch (final Exception e1) {
 					menuItem.setEnabled(false);
 				}
 			}
 		});
 		return menuItem;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(final String args[]) {
 		setDefaultLookAndFeelDecorated(true);
 
 		final JFrame frame = new JFrame();
 
 		final MetalTheme oldMetalTheme = getCurrentTheme();
 
 		final JMenu themeMenu = new JMenu();
 		final ButtonGroup themeMenuGroup = new ButtonGroup();
 		themeMenu.setText("Themes");
 		themeMenu.setMnemonic('T');
 		themeMenu.setEnabled(getLookAndFeel() instanceof MetalLookAndFeel);
 		for (final Class<? extends MetalTheme> clazz : listDescendants(MetalTheme.class, false, false, true, true, true)) {
 			try {
 				final JRadioButtonMenuItem menuItem = fromMetalTheme(clazz.newInstance(), frame);
 				themeMenu.add(menuItem);
 				themeMenuGroup.add(menuItem);
 			} catch (final InstantiationException | IllegalAccessException e) {
 				// ignore
 			}
 		}
 
 		final JMenu lookAndFeelMenu = new JMenu();
 		final ButtonGroup lookAndFeelMenuGroup = new ButtonGroup();
 		lookAndFeelMenu.setText("Look & Feel");
 		lookAndFeelMenu.setMnemonic('L');
 		final SortedSet<Class<? extends LookAndFeel>> descendants = listDescendants(LookAndFeel.class, false, false, true, true, true);
 		final List<Class<? extends LookAndFeel>> exclusions = asList(MultiLookAndFeel.class, SynthLookAndFeel.class);
 		descendants.removeAll(exclusions);
 		for (final Class<? extends LookAndFeel> clazz : descendants) {
 			try {
 				final JRadioButtonMenuItem menuItem = fromLookAndFeel(clazz.newInstance(), themeMenu, frame);
 				lookAndFeelMenu.add(menuItem);
 				lookAndFeelMenuGroup.add(menuItem);
 			} catch (final InstantiationException | IllegalAccessException e) {
 				// ignore
 			}
 		}
 
 		/*
 		 * Custom LaFs can steal the default metal theme when loaded,
 		 * so we're restoring it here.
 		 */
 		setCurrentTheme(oldMetalTheme);
 
 		final JMenuBar menuBar = new JMenuBar();
 		menuBar.add(lookAndFeelMenu);
 		menuBar.add(themeMenu);
 
 		frame.setJMenuBar(menuBar);
 		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		frame.setUndecorated(getLookAndFeel() instanceof MetalLookAndFeel);
 
 		final Container contentPane = frame.getContentPane();
 		contentPane.setPreferredSize(new Dimension(320, 240));
 		contentPane.setLayout(new BorderLayout());
 		contentPane.add(new JButton("-"), CENTER);
 
 		frame.pack();
 		frame.setVisible(true);
 
 		/*
 		 * Updates the theme menu by selecting the menu item which corresponds
 		 * to the currently selected metal theme.
 		 *
 		 * This is necessary because certain LaFs auto-refresh their themes on repaint.
 		 */
 		final Thread metalThemeChangeListener = new Thread("MetalThemeChangeListener") {
 			/**
 			 * @see Thread#run()
 			 */
 			@Override
 			public void run() {
 				while (!interrupted()) {
 					try {
 						if (getLookAndFeel() instanceof MetalLookAndFeel) {
 							invokeLater(new Runnable() {
 								/**
 								 * @see Runnable#run()
 								 */
 								@Override
 								public void run() {
 									final String themeClassName = getCurrentTheme().getClass().getName();
 									for (final AbstractButton button : list(themeMenuGroup.getElements())) {
 										if (themeClassName.equals(button.getName())) {
 											button.setSelected(true);
 											return;
 										}
 									}
 								}
 							});
 						}
 
 						/*
 						 * Once a second is more than enough.
 						 */
 						sleep(1000);
 					} catch (final InterruptedException ie) {
 						break;
 					}
 				}
 			}
 		};
 		metalThemeChangeListener.setDaemon(true);
 		metalThemeChangeListener.start();
 	}
 }
