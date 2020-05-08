 package org.scilab.forge.jlatexmath;
 
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 
 /**
  * Maintains a structure like MathML to convert JLaTeXMath into WYSIWYG
  * @author neel
  *
  */
 
 public class TreeEditor 
 {
 	protected Atom selAtom = null;
 	protected Atom root = null;
 	protected Atom treeParent = null;
 	protected TeXFormula formula = null;
 	protected boolean isWritingCommand = false;
 	protected StringBuffer command = new StringBuffer(); 
 	protected KeyEvent event = null;
 	protected static ArrayList<Atom> atoms = new ArrayList<Atom>();
 	protected int size = 18;
 	
 	public void keyTyped(TeXFormula formula, Atom r, char c)
 	{
 		this.formula = formula;
 		if(selAtom == null)
 			selAtom = r;
 		treeParent = selAtom.getTreeParent();
 		String com = null;
 		switch(c)
 		{
 			case '^' :
 				this.isWritingCommand = false;
 				this.makeScripts();
 				break;
 			case '/' :
 				this.isWritingCommand = false;
 				this.makeFraction();
 				break;
 			case '|' :
 				this.isWritingCommand = true;
 				if(command.length() > 1)
 					command.delete(0, command.length());
 				com = command.toString();
 				//this.symbolChar('|');
 				break;
 			case '' :
 				this.commandWritten(1);
 				break;
 			case '' :
 				this.commandWritten(2);
 				break;
 			default :
 				if(c == '' || event.isAltDown() || event.isControlDown())
 					return;
 				String latex = c + "";
 				this.symbolChar(latex);
 				if(this.isWritingCommand)
 				{
 					command.append(c);
 					com = command.toString();
 					int value = Commands.getValue(com);
 					if(value != 0)
 						this.commandWritten(value);
 				}
 		}
 	}
 	
 	public void keyPressed(TeXFormula formula, int n, KeyEvent event)
 	{
 		this.event = event;
 		this.formula = formula;
 		if(selAtom == null)
 			selAtom = formula.getRoot();
 		treeParent = selAtom.getTreeParent();
 		switch(n)
 		{
 		case 37 :
 			if(selAtom.getPrevSibling() != null)
 				selAtom = selAtom.getPrevSibling();
 			if(selAtom instanceof FencedAtom)
 			{
 				FencedAtom f = (FencedAtom) selAtom;
 				this.selAtom = f.getBase();
 			}
 			break;
 		case 38 :
 			 if(selAtom.getParent() != null)
 				 selAtom = selAtom.getParent();
 			 if(selAtom instanceof FencedAtom)
 			 {
 				 FencedAtom f = (FencedAtom) selAtom;
 				 this.selAtom = f.getBase();
 			 }	
 			 break;
 		case 39 :
 			if(selAtom.getNextSibling() != null)
 				selAtom = selAtom.getNextSibling();
 			if(selAtom instanceof FencedAtom)
 			{
 				FencedAtom f = (FencedAtom) selAtom;
 				this.selAtom = f.getBase();
 			}
 			break;
 		case 40 :
 			if(selAtom.getSubExpr() != null)
 				selAtom = selAtom.getSubExpr();
 			if(selAtom instanceof FencedAtom)
 			{
 				FencedAtom f = (FencedAtom) selAtom;
 				this.selAtom = f.getBase();
 			}
 			break;
 		case 8 :
 			if(selAtom != null)
 				this.delete();
 			break;
 		case 80 :
 			if(event.isAltDown())
 			{
 				String latex = "\\pi";
 				this.symbolChar(latex);
 			}
 			break;
 		case 65 :
 			if(event.isAltDown())
 			{
 				String latex = "\\alpha";
 				this.symbolChar(latex);
 			}
 			break;
 		case 66 :
 			if(event.isAltDown())
 			{
 				String latex = "\\beta";
 				this.symbolChar(latex);
 			}
 			break;
 		case 68 :
 			if(event.isAltDown())
 			{
 				String latex = "\\delta";
 				this.symbolChar(latex);
 			}
 			break;
 		case 70 :
 			if(event.isAltDown())
 			{
 				String latex = "\\phi";
 				this.symbolChar(latex);
 			}
 			break;
 		case 71 :
 			if(event.isAltDown())
 			{
 				String latex = "\\gamma";
 				this.symbolChar(latex);
 			}
 			break;	
 		case 76 :
 			if(event.isAltDown())
 			{
 				String latex = "\\lambda";
 				this.symbolChar(latex);
 			}
 			break;
 		case 77 :
 			if(event.isAltDown())
 			{
 				String latex = "\\mu";
 				this.symbolChar(latex);
 			}
 			break;
 		case 83 :
 			if(event.isAltDown())
 			{
 				String latex = "\\sigma";
 				this.symbolChar(latex);
 			}
 			break;
 		case 84 :
 			if(event.isAltDown())
 			{
 				String latex = "\\theta";
 				this.symbolChar(latex);
 			}
 			break;
 		case 87 :
 			if(event.isAltDown())
 			{
 				String latex = "\\omega";
 				this.symbolChar(latex);
 			}
 			break;
 		case 107 :
 			if(event.isAltDown())
 			{
 				String latex = "\\pm";
 				this.symbolChar(latex);
 			}
 			break;
 		case 109 :
 			if(event.isAltDown())
 			{
 				String latex = "\\mp";
 				this.symbolChar(latex);
 			}
 			break;
 		case 61 :
 			if(event.isAltDown())
 			{
 				String latex = "\\neq";
 				this.symbolChar(latex);
 			}
 			break;
 		case 44 :
 			if(event.isAltDown())
 			{
 				String latex = "\\leq";
 				this.symbolChar(latex);
 			}
 			break;
 		case 46 :
 			if(event.isAltDown())
 			{
 				String latex = "\\geq";
 				this.symbolChar(latex);
 			}
 			break;
 		case 81 :
 			if(event.isControlDown())
 			{
 				TeXFormula typed = new TeXFormula("\\sin");
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				TeXFormula fenced = new TeXFormula("\\left(\\right)");
 				FencedAtom f = (FencedAtom) fenced.getRoot();
 				f.setBase(e);
 				TrigoAtom trigo = new TrigoAtom((TypedAtom) typed.getRoot(), f); 
 				this.trigoFunction(trigo);
 				this.selAtom = e;
 			}
 			break;
 		default :
 			return;
 		}
 	}
 	
 	public void formulaClicked(double x, double y, TeXEnvironment te)
 	{
 		atoms.clear();
 		Box box = formula.getRoot().createBox(te);
 		box.updateRectangle(size, (3/size), (3/size));
 		int j = atoms.size();
 		int i = 0;
 		while(i != j)
 		{
 			Rectangle currentBox = atoms.get(i).getBox().getRectangle(); 
 			Rectangle rect = new Rectangle(currentBox.x + 178, currentBox.y + 178, currentBox.width + 2, currentBox.height + 2);
 			if(rect.contains(x, y))
 				this.selAtom = atoms.get(i);
 			++i;
 		}
 		if(selAtom instanceof FencedAtom)
 		{
 			FencedAtom f = (FencedAtom) selAtom;
 			this.selAtom = f.getBase();
 		}
 		atoms.clear();
 	}
 
 	public static void addAtoms(Atom at)
 	{
 		//System.out.println(at.getClass());
 		atoms.add(at);
 	}
 	
 	public void commandWritten(int value)
 	{
 		
 		switch(value)
 		{
 		case 1 :
 			this.isWritingCommand = false;
 			this.squareRoot();
 			break;
 		case 2 :
 			this.isWritingCommand = false;
 			this.nRoot();
 			break;
 		default :
 			return;
 		}	
 	}
 	
 	private void delete() 
 	{
 		if(selAtom instanceof EmptyAtom)
 		{
 			treeParent = selAtom.getTreeParent();
 			if(treeParent != null && treeParent instanceof ScriptsAtom)
 			{
 				ScriptsAtom s = (ScriptsAtom) treeParent;
 				Atom grandPa = s.getTreeParent();
 				if(grandPa != null && grandPa instanceof ScriptsAtom)
 				{
 					ScriptsAtom gs = (ScriptsAtom) grandPa;
 					if(treeParent.equals(gs.getBase()))
 					{
 						if(selAtom.equals(s.getBase()))
 							gs.setBase(s.getSuperScript());
 						else
 							gs.setBase(s.getBase());
 						this.selAtom = gs.getBase();
 					}
 					else
 					{
 						if(selAtom.equals(s.getBase()))
 							gs.setSuperScript(s.getSuperScript());
 						else
 							gs.setSuperScript(s.getBase());
 						this.selAtom = gs.getSuperScript();
 					}
 				}
 				if(grandPa != null && grandPa instanceof FractionAtom)
 				{
 					FractionAtom gf = (FractionAtom) grandPa;
 					if(treeParent.equals(gf.getNum()))
 					{
 						if(selAtom.equals(s.getBase()))
 							gf.setNum(s.getSuperScript());
 						else
 							gf.setNum(s.getBase());
 						this.selAtom = gf.getNum();
 					}
 					else
 					{
 						if(selAtom.equals(s.getBase()))
 							gf.setDenom(s.getSuperScript());
 						else
 							gf.setDenom(s.getBase());
 						this.selAtom = gf.getDenom();
 					}
 				}
 				if(grandPa != null && grandPa instanceof NthRoot)
 				{
 					NthRoot gn = (NthRoot) grandPa;
 					if(treeParent.equals(gn.getBase()))
 					{
 						if(selAtom.equals(s.getBase()))
 							gn.setBase(s.getSuperScript());
 						else
 							gn.setBase(s.getBase());
 						this.selAtom = gn.getBase();
 					}
 					else
 					{
 						if(selAtom.equals(s.getBase()))
 							gn.setRoot(s.getSuperScript());
 						else
 							gn.setRoot(s.getBase());
 						this.selAtom = gn.getRoot();
 					}
 
 				}
 				if(grandPa != null && grandPa instanceof RowAtom)
 				{
 					RowAtom gr = (RowAtom) grandPa;
 					int j = gr.elements.size();
 					int i = 0;
 					while(i != j)
 					{
 						if(treeParent.equals(gr.elements.get(i)))
 						{
 							gr.elements.remove(i);
 							break;
 						}
 						++i;
 					}
 					if(selAtom.equals(s.getBase()))
 					{
 						if(s.getSuperScript() instanceof RowAtom)
 						{
 							RowAtom temp = (RowAtom) s.getSuperScript();
 							int k = temp.elements.size();
 							int l = 0;
 							while(l != k)
 							{
 								gr.elements.add(i, temp.elements.get(l));
 								++i;
 								++l;
 							}
 							this.selAtom = temp.elements.get(k-1);
 						}
 						else
 						{
 							gr.elements.add(i, s.getSuperScript());
 							this.selAtom = s.getSuperScript();
 						}
 					}
 					else
 					{
 
 						if(s.getBase() instanceof RowAtom)
 						{
 							RowAtom temp = (RowAtom) s.getBase();
 							int k = temp.elements.size();
 							int l = 0;
 							while(l != k)
 							{
 								gr.elements.add(i, temp.elements.get(l));
 								++i;
 								++l;
 							}
 							this.selAtom = temp.elements.get(k-1);
 						}
 						else
 						{
 							gr.elements.add(i, s.getBase());
 							this.selAtom = s.getBase();
 						}
 					}
 				}
 				if(grandPa == null)
 				{
 					if(selAtom.equals(s.getBase()))
 						this.formula.setRoot(s.getSuperScript());
 					else
 						this.formula.setRoot(s.getBase());
 					this.selAtom = this.formula.getRoot();
 				}
 			}
 			if(treeParent != null && treeParent instanceof FractionAtom)
 			{
 				FractionAtom f = (FractionAtom) treeParent;
 				Atom grandPa = f.getTreeParent();
 				if(grandPa != null && grandPa instanceof ScriptsAtom)
 				{
 					ScriptsAtom gs = (ScriptsAtom) grandPa;
 					if(treeParent.equals(gs.getBase()))
 					{
 						if(selAtom.equals(f.getNum()))
 							gs.setBase(f.getDenom());
 						else
 							gs.setBase(f.getNum());
 						this.selAtom = gs.getBase();
 					}
 					else
 					{
 						if(selAtom.equals(f.getNum()))
 							gs.setSuperScript(f.getDenom());
 						else
 							gs.setSuperScript(f.getNum());
 						this.selAtom = gs.getSuperScript();
 					}
 				}
 				if(grandPa != null && grandPa instanceof FractionAtom)
 				{
 					FractionAtom gf = (FractionAtom) grandPa;
 					if(treeParent.equals(gf.getNum()))
 					{
 						if(selAtom.equals(f.getNum()))
 							gf.setNum(f.getDenom());
 						else
 							gf.setNum(f.getNum());
 						this.selAtom = gf.getNum();
 					}
 					else
 					{
 						if(selAtom.equals(f.getNum()))
 							gf.setDenom(f.getDenom());
 						else
 							gf.setDenom(f.getNum());
 						this.selAtom = gf.getDenom();
 					}
 				}
 				if(grandPa != null && grandPa instanceof NthRoot)
 				{
 					NthRoot gn = (NthRoot) grandPa;
 					if(treeParent.equals(gn.getBase()))
 					{
 						if(selAtom.equals(f.getNum()))
 							gn.setBase(f.getDenom());
 						else
 							gn.setBase(f.getNum());
 						this.selAtom = gn.getBase();
 					}
 					else
 					{
 						if(selAtom.equals(f.getNum()))
 							gn.setRoot(f.getDenom());
 						else
 							gn.setRoot(f.getNum());
 						this.selAtom = gn.getRoot();
 					}
 
 				}
 				if(grandPa != null && grandPa instanceof RowAtom)
 				{
 					RowAtom gr = (RowAtom) grandPa;
 					int j = gr.elements.size();
 					int i = 0;
 					while(i != j)
 					{
 						if(treeParent.equals(gr.elements.get(i)))
 						{
 							gr.elements.remove(i);
 							break;
 						}
 						++i;
 					}
 					if(selAtom.equals(f.getNum()))
 					{
 						if(f.getDenom() instanceof RowAtom)
 						{
 							RowAtom temp = (RowAtom) f.getDenom();
 							int k = temp.elements.size();
 							int l = 0;
 							while(l != k)
 							{
 								gr.elements.add(i, temp.elements.get(l));
 								++i;
 								++l;
 							}
 							this.selAtom = temp.elements.get(k-1);
 						}
 						else
 						{
 							gr.elements.add(i, f.getDenom());
 							this.selAtom = f.getDenom();
 						}
 					}
 					else
 					{
 						if(f.getNum() instanceof RowAtom)
 						{
 							RowAtom temp = (RowAtom) f.getNum();
 							int k = temp.elements.size();
 							int l = 0;
 							while(l != k)
 							{
 								gr.elements.add(i, temp.elements.get(l));
 								++i;
 								++l;
 							}
 							this.selAtom = temp.elements.get(k-1);
 						}
 						else
 						{
 							gr.elements.add(i, f.getNum());
 							this.selAtom = f.getNum();
 						}
 					}
 				}
 				if(grandPa == null)
 				{
 					if(selAtom.equals(f.getNum()))
 						this.formula.setRoot(f.getDenom());
 					else
 						this.formula.setRoot(f.getNum());
 					this.selAtom = this.formula.getRoot();
 				}
 			}
 			if(treeParent != null && treeParent instanceof NthRoot)
 			{
 				NthRoot n = (NthRoot) treeParent;
 				Atom grandPa = n.getTreeParent();
 				if(grandPa != null && grandPa instanceof ScriptsAtom)
 				{
 					ScriptsAtom gs = (ScriptsAtom) grandPa;
 					if(treeParent.equals(gs.getBase()))
 					{
 						EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 						if(selAtom.equals(n.getBase()))
 						{
 							if(n.getRoot() !=  null)
 								gs.setBase(n.getRoot());
 							else
 								gs.setBase(e);
 						}
 						else
 							gs.setBase(n.getBase());
 						this.selAtom = gs.getBase();
 					}
 					else
 					{
 						if(selAtom.equals(gs.getBase()))
 							gs.setSuperScript(n.getRoot());
 						else
 							gs.setSuperScript(n.getBase());
 						this.selAtom = gs.getSuperScript();
 					}
 				}
 				if(grandPa != null && grandPa instanceof FractionAtom)
 				{
 					FractionAtom gf = (FractionAtom) grandPa;
 					if(treeParent.equals(gf.getNum()))
 					{
 						EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 						if(selAtom.equals(n.getBase()))
 						{
 							if(n.getRoot() !=  null)
 								gf.setNum(n.getRoot());
 							else
 								gf.setNum(e);
 						}
 						else
 							gf.setNum(n.getBase());
 						this.selAtom = gf.getNum();
 					}
 					else
 					{
 						if(selAtom.equals(gf.getDenom()))
 							gf.setDenom(n.getRoot());
 						else
 							gf.setDenom(n.getBase());
 						this.selAtom = gf.getDenom();
 						
 					}
 				}
 				if(grandPa != null && grandPa instanceof NthRoot)
 				{
 					NthRoot gn = (NthRoot) grandPa;
 					if(treeParent.equals(gn.getBase()))
 					{
 						EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 						if(selAtom.equals(n.getBase()))
 						{
 							if(n.getRoot() !=  null)
 								gn.setBase(n.getRoot());
 							else
 								gn.setBase(e);
 						}
 						else
 							gn.setBase(n.getBase());
 						this.selAtom = gn.getBase();
 					}
 					else
 					{
 						if(selAtom.equals(n.getBase()))
 							gn.setRoot(n.getRoot());
 						else
 							gn.setRoot(n.getBase());
 						this.selAtom = gn.getBase();		 
 					}
 				}
 				if(grandPa != null && grandPa instanceof RowAtom)
 				{
 					RowAtom gr = (RowAtom) grandPa;
 					EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 					int j = gr.elements.size();
 					int i = 0;
 					while(i != j)
 					{
 						if(treeParent.equals(gr.elements.get(i)))
 						{
 							gr.elements.remove(i);
 							break;
 						}
 						++i;
 					}
 					if(selAtom.equals(n.getBase()))
 					{
 						if(n.getRoot() != null)
 						{
 							if(n.getRoot() instanceof RowAtom)
 							{
 								RowAtom r = (RowAtom) n.getRoot();
 								int k = r.elements.size();
 								int l = 0;
 								while(l != k)
 								{
 									gr.elements.add(i, r.elements.get(l));
 									++l;
 									++i;
 								}
 								this.selAtom = r.elements.get(l-1);
 							}
 							else
 							{
 								gr.elements.add(i, n.getRoot());
 								this.selAtom = n.getRoot();
 							}
 						}
 						else
 						{
 							gr.elements.add(i, e);
 							this.selAtom = e;
 						}
 					}
 					else
 					{
 						if(n.getBase() instanceof RowAtom)
 						{
 							RowAtom r = (RowAtom) n.getBase();
 							int k = r.elements.size();
 							int l = 0;
 							while(l != k)
 							{
 								gr.elements.add(i, r.elements.get(l));
 								++l;
 								++i;
 							}
 							this.selAtom = r.elements.get(l-1);
 						}
 						else
 						{
 							gr.elements.add(i, n.getBase());
 							this.selAtom = n.getBase();
 						}
 					}
 				}
 				if(grandPa == null)
 				{
 					EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 					if(selAtom.equals(n.getBase()))
 					{
 						if(n.getRoot() != null)
 						{
 							this.formula.setRoot(n.getRoot());
 							this.selAtom = n.getRoot();
 						}
 						else
 						{
 							this.formula.setRoot(e);
 							this.selAtom = e;
 						}
 					}
 					else
 					{
 						this.formula.setRoot(n.getBase());
 						this.selAtom = n.getBase();
 					}
 				}
 			}
 			if(treeParent != null && treeParent instanceof RowAtom)
 			{
 				RowAtom r = (RowAtom) treeParent;
 				int j = r.elements.size();
 				int i = 0;
 				while(i != j)
 				{
 					if(selAtom.equals(r.elements.get(i)))
 					{
 						r.elements.remove(i);
 						break;
 					}
 					++i;
 				}
 				if(j > 2)
 				{
 					if(i == j-1)
 						this.selAtom = r.elements.get(i-1);
 					else
 						this.selAtom = r.elements.get(i);
 				}
 				else
 				{
 					Atom grandPa = treeParent.getTreeParent();
 					if(grandPa != null && grandPa instanceof ScriptsAtom)
 					{
 						ScriptsAtom gs = (ScriptsAtom) grandPa;
 						if(treeParent.equals(gs.getBase()))
 						{
 							gs.setBase(r.elements.getFirst());
 							this.selAtom = gs.getBase();
 						}
 						else
 						{
 							gs.setSuperScript(r.elements.getFirst());
 							this.selAtom = gs.getSuperScript();
 						}
 					}
 					if(grandPa != null && grandPa instanceof FractionAtom)
 					{
 						FractionAtom gf = (FractionAtom) grandPa;
 						if(treeParent.equals(gf.getNum()))
 						{
 							gf.setNum(r.elements.getFirst());
 							this.selAtom = gf.getNum();
 						}
 						else
 						{
 							gf.setDenom(r.elements.getFirst());
 							this.selAtom = gf.getDenom();
 						}
 					}
 					if(grandPa != null && grandPa instanceof NthRoot)
 					{
 						NthRoot gn = (NthRoot) grandPa;
 						if(treeParent.equals(gn.getBase()))
 							gn.setBase(r.elements.getFirst());
 						else
 							gn.setRoot(r.elements.getFirst());
 						this.selAtom = r.elements.getFirst();
 					}
 					if(grandPa != null && grandPa instanceof FencedAtom)
 					{
 						FencedAtom f = (FencedAtom) grandPa;
 						if(grandPa.getTreeParent() instanceof TrigoAtom)
 						{
 							f.setBase(r.elements.getFirst());
 							this.selAtom = f.getBase();
 						}
 						else
 						{
 							ScriptsAtom s = (ScriptsAtom) grandPa.getTreeParent();
 							s.setBase(r.elements.getFirst());
 							this.selAtom = s.getBase();
 						}
 					}
 					if(grandPa == null)
 					{
 						this.formula.setRoot(r.elements.getFirst());
 						//this.formula.createBox(this.formula.style);
 						this.selAtom = this.formula.getRoot();
 					}
 				}
 			}
 			if(treeParent != null && treeParent instanceof FencedAtom)
 			{
 				FencedAtom n = (FencedAtom) treeParent;
 				Atom grandPa = n.getTreeParent().getTreeParent();
 				treeParent = selAtom.getTreeParent().getTreeParent();
 				if(grandPa != null && grandPa instanceof ScriptsAtom)
 				{
 					ScriptsAtom gs = (ScriptsAtom) grandPa;
 					EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 					if(treeParent.equals(gs.getBase()))
 					{
 						gs.setBase(e);
 						this.selAtom = e;
 					}
 					else
 					{
 						gs.setSuperScript(e);
 						this.selAtom = e;
 					}
 				}
 				if(grandPa != null && grandPa instanceof FractionAtom)
 				{
 					FractionAtom gf = (FractionAtom) grandPa;
 					EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 					if(treeParent.equals(gf.getNum()))
 					{
 						gf.setNum(e);
 						this.selAtom = e;
 					}
 					else
 					{
 						gf.setDenom(e);
 						this.selAtom = e;
 					}
 				}
 				if(grandPa != null && grandPa instanceof NthRoot)
 				{
 					NthRoot gn = (NthRoot) grandPa;
 					EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 					if(treeParent.equals(gn.getBase()))
 					{
 						gn.setBase(e);
 						this.selAtom = e;
 					}
 					else
 					{
 						gn.setRoot(e);
 						this.selAtom = e;
 					}
 				}
 				if(grandPa != null && grandPa instanceof RowAtom)
 				{
 					RowAtom gr = (RowAtom) grandPa;
 					EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 					int j = gr.elements.size();
 					int i = 0;
 					while(i != j)
 					{
 						if(treeParent.equals(gr.elements.get(i)))
 						{
 							gr.elements.remove(i);
 							break;
 						}
 						++i;
 					}
 					gr.elements.add(i, e);
 					this.selAtom = e;
 				}
 				if(grandPa == null)
 				{
 					EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 					this.formula.setRoot(e);
 					this.selAtom = e;
 				}
 			}
 		}
 		else
 		{
 			treeParent = selAtom.getTreeParent();
 			if(treeParent != null && treeParent instanceof ScriptsAtom)
 			{
 				ScriptsAtom s = (ScriptsAtom) treeParent;
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				if(selAtom.equals(s.getBase()))
 					s.setBase(e);
 				else
 					s.setSuperScript(e);
 				this.selAtom = e;
 			}
 			if(treeParent != null && treeParent instanceof FractionAtom)
 			{
 				FractionAtom f = (FractionAtom) treeParent;
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				if(selAtom.equals(f.getNum()))
 					f.setNum(e);
 				else
 					f.setDenom(e);
 				this.selAtom = e;
 			}
 			if(treeParent != null && treeParent instanceof NthRoot)
 			{
 				NthRoot n = (NthRoot) treeParent;
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				if(selAtom.equals(n.getBase()))
 					n.setBase(e);
 				else
 					n.setRoot(e);
 
 				this.selAtom = e;
 			}
 			if(treeParent != null && treeParent instanceof RowAtom)
 			{
 				RowAtom r = (RowAtom) treeParent;
 				int j = r.elements.size();
 				int i = 0;
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				while(i != j)
 				{
 					if(selAtom.equals(r.elements.get(i)))
 					{
 						r.elements.remove(i);
 						break;	
 					}
 					++i;
 				}
 				r.elements.add(i, e);
 				this.selAtom = e;
 			}
 			if(treeParent != null && treeParent instanceof FencedAtom)
 			{
 				FencedAtom f = (FencedAtom) treeParent;
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				if(f.getTreeParent() instanceof TrigoAtom)
 					f.setBase(e);
 				else
 				{
 					ScriptsAtom s = (ScriptsAtom) f.getTreeParent();
 					s.setBase(e);
 				}
 				this.selAtom = e;
 			}
 			if(treeParent == null)
 			{
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				this.formula.setRoot(e);
 				this.formula.createBox(this.formula.style);
 				this.selAtom = e;
 			}
 		}
 	}
 	
 	public void makeScripts()
 	{
 		Atom at = this.selAtom;
 		if(!(selAtom instanceof CharAtom || selAtom instanceof SymbolAtom))
 		{
 			TeXFormula temp = new TeXFormula("\\left(\\right)"); 
 			FencedAtom f = (FencedAtom) temp.getRoot();
 			f.setBase(selAtom);
 			this.selAtom = f;
 		}
 		if(treeParent != null && treeParent instanceof ScriptsAtom)
 		{
 			ScriptsAtom temp = (ScriptsAtom) treeParent;
 			EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 			ScriptsAtom s = new ScriptsAtom(selAtom, null, e);
 			if(selAtom.equals(temp.getBase()))
 			{
 				TeXFormula temp2 = new TeXFormula("\\left(\\right)"); 
 				FencedAtom f = (FencedAtom) temp2.getRoot();
 				f.setBase(s);
 				temp.setBase(f);
 			}
 			else
 				temp.setSuperScript(s);
 			this.selAtom = e;
 		}
 		if(treeParent != null && treeParent instanceof FractionAtom)
 		{
 			FractionAtom temp = (FractionAtom) treeParent;
 			EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 			ScriptsAtom s = new ScriptsAtom(selAtom, null, e);
 			if(selAtom.equals(temp.getNum()))
 				temp.setNum(s);
 			else
 				temp.setDenom(s);
 			this.selAtom = e;
 		}
 		if(treeParent != null && treeParent instanceof NthRoot)
 		{
 			NthRoot temp = (NthRoot) treeParent;
 			EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 			ScriptsAtom s = new ScriptsAtom(selAtom, null, e);
 			if(selAtom.equals(temp.getBase()))
 				temp.setBase(s);
 			else
 				temp.setRoot(s);
 			this.selAtom = e;
 		}
 		if(treeParent != null && treeParent instanceof RowAtom)
 		{
 			RowAtom temp = (RowAtom) treeParent;
 			int j = temp.elements.size();
 			int i = 0;
 			EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 			ScriptsAtom s = new ScriptsAtom(selAtom, null, e);
 			while(i != j)
 			{
 				if(at.equals(temp.elements.get(i)))
 				{
 					temp.elements.remove(i);
 					break;
 				}
 				++i;
 			}
 			temp.elements.add(i, s);
 			this.selAtom = e;
 		}
 		if(treeParent != null && treeParent instanceof FencedAtom)
 		{
 			FencedAtom f = (FencedAtom) treeParent;
 			EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 			ScriptsAtom s = new ScriptsAtom(selAtom, null, e);
 			f.setBase(s);
 			this.selAtom = e;
 		}
 		if(treeParent == null)
 		{
 			EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 			ScriptsAtom s = new ScriptsAtom(selAtom, null, e);
 			formula.setRoot(s);
 			this.selAtom = e;
 		}
 	}
 	
 	public void makeFraction()
 	{
 		if(treeParent != null && treeParent instanceof ScriptsAtom)
 		{
 			ScriptsAtom temp = (ScriptsAtom) treeParent;
 			EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 			FractionAtom f = new FractionAtom(selAtom, e);
 			if(selAtom.equals(temp.getBase()))
 			{
 				TeXFormula temp2 = new TeXFormula("\\left(\\right)"); 
 				FencedAtom fo = (FencedAtom) temp2.getRoot();
 				fo.setBase(f);
 				temp.setBase(fo);
 			}
 			else
 				temp.setSuperScript(f);
 			this.selAtom = e;
 		}
 		if(treeParent != null && treeParent instanceof FractionAtom)
 		{
 			FractionAtom temp = (FractionAtom) treeParent;
 			EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 			FractionAtom f = new FractionAtom(selAtom, e);
 			if(selAtom.equals(temp.getNum()))
 				temp.setNum(f);
 			else
 				temp.setDenom(f);
 			this.selAtom = e;
 		}
 		if(treeParent != null && treeParent instanceof NthRoot)
 		{
 			NthRoot temp = (NthRoot) treeParent;
 			EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 			FractionAtom f = new FractionAtom(selAtom, e);
 			if(selAtom.equals(temp.getBase()))
 				temp.setBase(f);
 			else
 				temp.setRoot(f);
 			this.selAtom = e;
 		}
 		if(treeParent != null && treeParent instanceof RowAtom)
 		{
 			RowAtom temp = (RowAtom) treeParent;
 			int j = temp.elements.size();
 			int i = 0;
 			EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 			FractionAtom f = new FractionAtom(selAtom, e);
 			while(i != j)
 			{
 				if(selAtom.equals(temp.elements.get(i)))
 				{
 					temp.elements.remove(i);
 					break;
 				}
 				++i;
 			}
 			temp.elements.add(i, f);
 			this.selAtom = e;
 		}
 		if(treeParent != null && treeParent instanceof FencedAtom)
 		{
 			FencedAtom f = (FencedAtom) treeParent;
 			EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 			FractionAtom fr = new FractionAtom(selAtom, e);
 			f.setBase(fr);
 			this.selAtom = e;
 		}
 		if(treeParent == null)
 		{
 			EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 			FractionAtom f = new FractionAtom(selAtom, e);
 			formula.setRoot(f);
 			this.selAtom = e;
 		}
 	}
 	
 	public void symbolChar(String latex)
 	{
 		if(selAtom instanceof EmptyAtom)
 		{
 			if(treeParent != null && treeParent instanceof ScriptsAtom)
 			{
 				ScriptsAtom temp = (ScriptsAtom) treeParent;
 				TeXFormula formula = new TeXFormula(latex);
 				Atom root = formula.getRoot();
 				if(selAtom.equals(temp.getBase()))
 					temp.setBase(root);
 				else
 					temp.setSuperScript(root);
 				this.selAtom = root;
 			}
 			if(treeParent != null && treeParent instanceof FractionAtom)
 			{
 				FractionAtom temp = (FractionAtom) treeParent;
 				TeXFormula formula = new TeXFormula(latex);
 				Atom root = formula.getRoot();
 				if(selAtom.equals(temp.getNum()))
 					temp.setNum(root);
 				else
 					temp.setDenom(root);
 				this.selAtom = root;
 			}
 			if(treeParent != null && treeParent instanceof NthRoot)
 			{
 				NthRoot temp = (NthRoot) treeParent;
 				TeXFormula formula = new TeXFormula(latex);
 				Atom root = formula.getRoot();
 				if(selAtom.equals(temp.getBase()))
 					temp.setBase(root);
 				else
 					temp.setRoot(root);
 				this.selAtom = root;
 			}
 			if(treeParent != null && treeParent instanceof RowAtom)
 			{
 				RowAtom temp = (RowAtom) treeParent;
 				int j = temp.elements.size();
 				int i = 0;
 				TeXFormula formula = new TeXFormula(latex);
 				Atom root = formula.getRoot();
 				while(i != j)
 				{
 					if(selAtom.equals(temp.elements.get(i)))
 					{
 						temp.elements.remove(i);
 						break;
 					}
 					++i;
 				}
 				temp.elements.add(i, root);
 				this.selAtom = root;
 			}
 			if(treeParent != null && treeParent instanceof FencedAtom)
 			{
 				FencedAtom f = (FencedAtom) treeParent;
 				TeXFormula formula = new TeXFormula(latex);
 				Atom root = formula.getRoot();
 				f.setBase(root);
 				this.selAtom = root;
 			}
 			if(treeParent == null)
 			{
 				TeXFormula formula = new TeXFormula(latex);
 				Atom root = formula.getRoot();
 				this.formula.setRoot(root);
 				this.selAtom = root;
 			}
 		}
 		else
 		{
 			if(treeParent != null && treeParent instanceof ScriptsAtom)
 			{
 				ScriptsAtom s = (ScriptsAtom) treeParent;
 				TeXFormula formula = new TeXFormula(latex);
 				Atom root = formula.getRoot();
 				if(selAtom instanceof RowAtom)
 				{
 					RowAtom temp = (RowAtom) selAtom;
 					temp.elements.addLast(root);
 				}
 				else
 				{
 					RowAtom temp = new RowAtom(selAtom);
 					temp.add(root);
 					if(selAtom.equals(s.getBase()))
 					{
 						TeXFormula temp2 = new TeXFormula("\\left(\\right)"); 
 						FencedAtom f = (FencedAtom) temp2.getRoot();
 						f.setBase(temp);
 						s.setBase(f);
 					}
 					else
 						s.setSuperScript(temp);
 				}
 				this.selAtom = root;
 			}
 			if(treeParent != null && treeParent instanceof FractionAtom)
 			{
 				FractionAtom f = (FractionAtom) treeParent;
 				TeXFormula formula = new TeXFormula(latex);
 				Atom root = formula.getRoot();
 				if(selAtom instanceof RowAtom)
 				{
 					RowAtom temp = (RowAtom) selAtom;
 					temp.elements.addLast(root);
 				}
 				else
 				{
 					RowAtom temp = new RowAtom(selAtom);
 					temp.add(root);
 					if(selAtom.equals(f.getNum()))
 						f.setNum(temp);
 					else
 						f.setDenom(temp);
 				}
 				this.selAtom = root;
 			}
 			if(treeParent != null && treeParent instanceof NthRoot)
 			{
 				NthRoot n = (NthRoot) treeParent;
 				TeXFormula formula = new TeXFormula(latex);
 				Atom root = formula.getRoot();;
 				if(selAtom instanceof RowAtom)
 				{
 					RowAtom temp = (RowAtom) selAtom;
 					temp.elements.addLast(root);
 				}
 				else
 				{
 					RowAtom temp = new RowAtom(selAtom);
 					temp.add(root);
 					if(selAtom.equals(n.getBase()))
 						n.setBase(temp);
 					else
 						n.setRoot(temp);
 				}
 				this.selAtom = root;
 			}
 			if(treeParent != null && treeParent instanceof RowAtom)
 			{
 				RowAtom r = (RowAtom) treeParent;
 				int j = r.elements.size();
 				int i = 0;
 				TeXFormula formula = new TeXFormula(latex);
 				Atom root = formula.getRoot();
 				while(i != j)
 				{
 					if(selAtom.equals(r.elements.get(i)))
 						break;
 					++i;
 				}
 				r.elements.add(i+1,root);
 				this.selAtom = root;
 			}
 			if(treeParent != null && treeParent instanceof FencedAtom)
 			{
 				FencedAtom f = (FencedAtom) treeParent;
 				TeXFormula formula = new TeXFormula(latex);
 				Atom root = formula.getRoot();
 				if(selAtom instanceof RowAtom)
 				{
 					RowAtom r = (RowAtom) selAtom;
 					r.elements.addLast(root);
 				}
 				else
 				{
 					RowAtom r = new RowAtom(selAtom);
 					r.add(root);
 					f.setBase(r);
 				}
 				this.selAtom = root;
 			}
 			if(treeParent == null)
 			{
 				TeXFormula formula = new TeXFormula(latex);
 				Atom root = formula.getRoot();
 				if(selAtom instanceof RowAtom)
 				{
 					RowAtom r = (RowAtom) selAtom;
 					r.elements.addLast(root);
 				}
 				else
 				{
 					RowAtom r = new RowAtom(selAtom);
 					r.add(root);
 					this.formula.setRoot(r);
 				}
 				this.selAtom = root;
 			}
 		}
 	}
 	
 	public void squareRoot()
 	{
 		treeParent = selAtom.getTreeParent();
 		if(selAtom instanceof EmptyAtom)
 		{
 			if(treeParent != null && treeParent instanceof ScriptsAtom)
 			{
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				NthRoot n = new NthRoot(e, null);
 				ScriptsAtom temp = (ScriptsAtom) treeParent;
 				if(selAtom.equals(temp.getBase()))
 				{
 					TeXFormula temp2 = new TeXFormula("\\left(\\right)"); 
 					FencedAtom f = (FencedAtom) temp2.getRoot();
 					f.setBase(n);
 					temp.setBase(f);
 				}
 				else
 					temp.setSuperScript(n);
 				this.selAtom = e;
 			}
 			if(treeParent != null && treeParent instanceof FractionAtom)
 			{
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				NthRoot n = new NthRoot(e, null);
 				FractionAtom temp = (FractionAtom) treeParent;
 				if(selAtom.equals(temp.getNum()))
 					temp.setNum(n);
 				else
 					temp.setDenom(n);
 				this.selAtom = e;
 			}
 			if(treeParent != null && treeParent instanceof NthRoot)
 			{
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				NthRoot n = new NthRoot(e, null);
 				NthRoot temp = (NthRoot) treeParent;
 				if(selAtom.equals(temp.getBase()))
 					temp.setBase(n);
 				else
 					temp.setRoot(n);
 				this.selAtom = e;
 			}
 			if(treeParent != null && treeParent instanceof RowAtom)
 			{
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				NthRoot n = new NthRoot(e, null);
 				RowAtom temp = (RowAtom) treeParent;
 				int j = temp.elements.size();
 				int i = 0;
 				while(i != j)
 				{
 					if(selAtom.equals(temp.elements.get(i)))
 					{
 						temp.elements.remove(i);
 						break;
 					}
 					++i;
 				}
 				temp.elements.add(i, n);
 				this.selAtom = e;
 			}
 			if(treeParent != null && treeParent instanceof FencedAtom)
 			{
 				FencedAtom f = (FencedAtom) treeParent;
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				NthRoot n = new NthRoot(e, null);
 				f.setBase(n);
 				this.selAtom = e;
 			}
 			if(treeParent == null)
 			{
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				NthRoot n = new NthRoot(e, null);
 				this.formula.setRoot(n);
 				this.selAtom = e;
 			}
 		}
 		else
 		{
 			if(treeParent != null && treeParent instanceof ScriptsAtom)
 			{
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				NthRoot n = new NthRoot(e, null);
 				ScriptsAtom s = (ScriptsAtom) treeParent;
 				if(selAtom instanceof RowAtom)
 				{
 					RowAtom temp = (RowAtom) selAtom;
 					temp.elements.addLast(n);
 				}
 				else
 				{
 					RowAtom temp = new RowAtom(selAtom);
 					temp.add(n);
 					if(selAtom.equals(s.getBase()))
 						s.setBase(temp);
 					else
 						s.setSuperScript(temp);
 				}
 				this.selAtom = e;
 			}
 			if(treeParent != null && treeParent instanceof FractionAtom)
 			{
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				NthRoot n = new NthRoot(e, null);
 				FractionAtom f = (FractionAtom) treeParent;
 				if(selAtom instanceof RowAtom)
 				{
 					RowAtom temp = (RowAtom) selAtom;
 					temp.elements.addLast(n);
 				}
 				else
 				{
 					RowAtom temp = new RowAtom(selAtom);
 					temp.add(n);
 					if(selAtom.equals(f.getNum()))
 						f.setNum(temp);
 					else
 						f.setDenom(temp);
 				}
 				this.selAtom = e;
 			}
 			if(treeParent != null && treeParent instanceof NthRoot)
 			{
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				NthRoot n = new NthRoot(e, null);
 				NthRoot tn = (NthRoot) treeParent;
 				if(selAtom instanceof RowAtom)
 				{
 					RowAtom temp = (RowAtom) selAtom;
 					temp.elements.addLast(n);
 				}
 				else
 				{
 					RowAtom temp = new RowAtom(selAtom);
 					temp.add(n);
 					if(selAtom.equals(tn.getBase()))
 						tn.setBase(temp);
 					else
 						tn.setRoot(temp);
 				}
 				this.selAtom = e;
 			}
 			if(treeParent != null && treeParent instanceof RowAtom)
 			{
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				NthRoot n = new NthRoot(e, null);
 				RowAtom r = (RowAtom) treeParent;
 				int j = r.elements.size();
 				int i = 0;
 				while(i != j)
 				{
 					if(selAtom.equals(r.elements.get(i)))
 						break;
 					++i;
 				}
 				r.elements.add(i+1,n);
 				this.selAtom = e;
 			}
 			if(treeParent != null && treeParent instanceof FencedAtom)
 			{
 				FencedAtom f = (FencedAtom) treeParent;
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				NthRoot n = new NthRoot(e, null);
 				if(selAtom instanceof RowAtom)
 				{
 					RowAtom r = (RowAtom) selAtom;
 					r.elements.addLast(n);
 				}
 				else
 				{
 					RowAtom r = new RowAtom(selAtom);
 					r.add(n);
 					f.setBase(r);
 				}
 				this.selAtom = e;
 			}
 			if(treeParent == null)
 			{
 				EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 				NthRoot n = new NthRoot(e, null);
 				if(selAtom instanceof RowAtom)
 				{
 					RowAtom r = (RowAtom) selAtom;
 					r.elements.addLast(n);
 				}
 				else
 				{
 					RowAtom r = new RowAtom(selAtom);
 					r.add(n);
 					this.formula.setRoot(r);
 				}
 				this.selAtom = e;
 			}
 		}
 	}
 	public void nRoot()
 	{
 		treeParent = selAtom.getTreeParent();
 		if(treeParent != null && treeParent instanceof ScriptsAtom)
 		{
 			ScriptsAtom s = (ScriptsAtom) treeParent;
 			EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 			NthRoot n = new NthRoot(e, selAtom);
 			if(selAtom.equals(s.getBase()))
 				s.setBase(n);
 			else
 				s.setSuperScript(n);
 			this.selAtom = e;
 		}
 		if(treeParent != null && treeParent instanceof FractionAtom)
 		{
 			FractionAtom f = (FractionAtom) treeParent;
 			EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 			NthRoot n = new NthRoot(e, selAtom);
 			if(selAtom.equals(f.getNum()))
 				f.setNum(n);
 			else
 				f.setDenom(n);
 			this.selAtom = e;
 		}
 		if(treeParent != null && treeParent instanceof NthRoot)
 		{
 			NthRoot tn = (NthRoot) treeParent;
 			EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 			NthRoot n = new NthRoot(e, selAtom);
 			if(selAtom.equals(tn.getBase()))
 				tn.setBase(n);
 			else
 				tn.setRoot(n);
 			this.selAtom = e;
 		}
 		if(treeParent != null && treeParent instanceof RowAtom)
 		{
 			RowAtom r = (RowAtom) treeParent;
 			int j = r.elements.size();
 			int i = 0;
 			EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 			NthRoot n = new NthRoot(e, selAtom);
 			while(i != j)
 			{
 				if(selAtom.equals(r.elements.get(i)))
 				{
 					r.elements.remove(i);
 					break;
 				}
 				++i;
 			}
 			r.elements.add(i, n);
 			this.selAtom = e;
 		}
 		if(treeParent != null && treeParent instanceof FencedAtom)
 		{
 			FencedAtom f = (FencedAtom) treeParent;
 			EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 			NthRoot n = new NthRoot(e, selAtom);
 			f.setBase(n);
 			this.selAtom = e;			
 		}
 		if(treeParent == null)
 		{
 			EmptyAtom e = new EmptyAtom(0.5f, 0.5f, 0, 0);
 			NthRoot n = new NthRoot(e, selAtom);
 			this.formula.setRoot(n);
 			this.selAtom = e;
 		}
 	}
 	
 	public void trigoFunction(TrigoAtom trigo)
 	{
 		if(selAtom instanceof EmptyAtom)
 		{
 			if(treeParent != null && treeParent instanceof ScriptsAtom)
 			{
 				ScriptsAtom temp = (ScriptsAtom) treeParent;
 				if(selAtom.equals(temp.getBase()))
 					temp.setBase(trigo);
 				else
 					temp.setSuperScript(trigo);
 			}
 			if(treeParent != null && treeParent instanceof FractionAtom)
 			{
 				FractionAtom temp = (FractionAtom) treeParent;
 				if(selAtom.equals(temp.getNum()))
 					temp.setNum(trigo);
 				else
 					temp.setDenom(trigo);
 			}
 			if(treeParent != null && treeParent instanceof NthRoot)
 			{
 				NthRoot temp = (NthRoot) treeParent;
 				if(selAtom.equals(temp.getBase()))
 					temp.setBase(trigo);
 				else
 					temp.setRoot(trigo);
 			}
 			if(treeParent != null && treeParent instanceof RowAtom)
 			{
 				RowAtom temp = (RowAtom) treeParent;
 				int j = temp.elements.size();
 				int i = 0;
 				while(i != j)
 				{
 					if(selAtom.equals(temp.elements.get(i)))
 					{
 						temp.elements.remove(i);
 						break;
 					}
 					++i;
 				}
 				temp.elements.add(i, trigo);
 			}
 			if(treeParent != null && treeParent instanceof FencedAtom)
 			{
 				FencedAtom f = (FencedAtom) treeParent;
 				f.setBase(trigo);
 			}
 			if(treeParent == null)
 				this.formula.setRoot(trigo);
 		}
 		else
 		{
 			if(treeParent != null && treeParent instanceof ScriptsAtom)
 			{
 				ScriptsAtom s = (ScriptsAtom) treeParent;
 				if(selAtom instanceof RowAtom)
 				{
 					RowAtom temp = (RowAtom) selAtom;
 					temp.elements.addLast(trigo);
 				}
 				else
 				{
 					RowAtom temp = new RowAtom(selAtom);
 					temp.add(trigo);
 					if(selAtom.equals(s.getBase()))
 						s.setBase(temp);
 					else
 						s.setSuperScript(temp);
 				}
 			}
 			if(treeParent != null && treeParent instanceof FractionAtom)
 			{
 				FractionAtom f = (FractionAtom) treeParent;
 				if(selAtom instanceof RowAtom)
 				{
 					RowAtom temp = (RowAtom) selAtom;
 					temp.elements.addLast(trigo);
 				}
 				else
 				{
 					RowAtom temp = new RowAtom(selAtom);
 					temp.add(trigo);
 					if(selAtom.equals(f.getNum()))
 						f.setNum(temp);
 					else
 						f.setDenom(temp);
 				}
 			}
 			if(treeParent != null && treeParent instanceof NthRoot)
 			{
 				NthRoot n = (NthRoot) treeParent;
 				if(selAtom instanceof RowAtom)
 				{
 					RowAtom temp = (RowAtom) selAtom;
 					temp.elements.addLast(trigo);
 				}
 				else
 				{
 					RowAtom temp = new RowAtom(selAtom);
 					temp.add(trigo);
 					if(selAtom.equals(n.getBase()))
 						n.setBase(temp);
 					else
 						n.setRoot(temp);
 				}
 				this.selAtom = root;
 			}
 			if(treeParent != null && treeParent instanceof RowAtom)
 			{
 				RowAtom r = (RowAtom) treeParent;
 				int j = r.elements.size();
 				int i = 0;
 				while(i != j)
 				{
 					if(selAtom.equals(r.elements.get(i)))
 						break;
 					++i;
 				}
 				r.elements.add(i+1, trigo);
 			}
 			if(treeParent != null && treeParent instanceof FencedAtom)
 			{
 				FencedAtom f = (FencedAtom) treeParent;
 				if(selAtom instanceof RowAtom)
 				{
 					RowAtom r = (RowAtom) selAtom;
 					r.elements.addFirst(trigo);
 				}
 				else
 				{
 					RowAtom temp = new RowAtom(selAtom);
 					temp.add(trigo);
 					f.setBase(temp);
 				}
 			}
 			if(treeParent == null)
 			{
 				if(selAtom instanceof RowAtom)
 				{
 					RowAtom r = (RowAtom) selAtom;
 					r.elements.addLast(trigo);
 				}
 				else
 				{
 					RowAtom r = new RowAtom(selAtom);
 					r.add(trigo);
 					this.formula.setRoot(r);
 				}
 			}
 		}
 	}
 	
 	public Atom getSelAtm()
 	{
 		return selAtom;
 	}
 	
 	public void initFormula(TeXFormula formula)
 	{
 		this.formula = formula;
 	}
 }
