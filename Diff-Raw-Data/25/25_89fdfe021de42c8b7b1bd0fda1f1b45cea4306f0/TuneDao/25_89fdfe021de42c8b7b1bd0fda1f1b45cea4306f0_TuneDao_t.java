 package org.soldomi.model.tune2;
 
 import org.soldomi.commons2.DaoAction;
 import org.soldomi.commons2.Result;
 import org.soldomi.commons2.SqlInsert;
 import org.soldomi.commons2.SqlSelect;
 import org.soldomi.commons2.ParameterSet;
 import org.soldomi.commons2.FieldSet;
 import org.soldomi.commons2.Function8;
 import org.soldomi.commons2.Function7;
 import org.soldomi.commons2.Function6;
 import org.soldomi.commons2.Function5;
 import org.soldomi.commons2.Function4;
 import org.soldomi.commons2.Function3;
 import org.soldomi.commons2.Function2;
 import org.soldomi.commons2.Function1;
 import org.soldomi.commons2.RowParser;
 import org.soldomi.commons2.CollectionUtils;
 import org.soldomi.commons2.Reducer;
 
 import org.apache.commons.math3.fraction.Fraction;
 
 import java.sql.Connection;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 
 public class TuneDao {
     public static final DaoAction<Tune, Tune> insertTune = new SqlInsert<Tune>("INSERT INTO tune (name) VALUES (?)") {
 	@Override public ParameterSet withParameterSet(Tune tune) {
 	    return new ParameterSet().add(tune.name);
 	}
 	@Override public Tune withKey(Tune tune, Long id) {
 	    return tune.withId(id);
 	}
     };
 
     public static final DaoAction<Syst, Syst> insertSyst = new SqlInsert<Syst>("INSERT INTO syst (tune_id, name) VALUES (?, ?)") {
 	@Override public ParameterSet withParameterSet(Syst syst) {
 	    return new ParameterSet()
 	    .add(syst.tuneId)
 	    .add(syst.name);
 	}
 	@Override public Syst withKey(Syst syst, Long id) {
 	    return syst.withId(id);
 	}
     };
 
     public static final DaoAction<Staff, Staff> insertStaff = new SqlInsert<Staff>("INSERT INTO staff (syst_id, name) VALUES (?, ?)") {
 	@Override public ParameterSet withParameterSet(Staff staff) {
 	    return new ParameterSet()
 	    .add(staff.systId)
 	    .add(staff.name);
 	}
 	@Override public Staff withKey(Staff staff, Long id) {
 	    return staff.withId(id);
 	}
     };
 
     public static final DaoAction<Sect, Sect> insertSect = new SqlInsert<Sect>("INSERT INTO sect (tune_id, start_time) VALUES (?, ?)") {
 	@Override public ParameterSet withParameterSet(Sect sect) {
 	    return new ParameterSet()
 	    .add(sect.tuneId)
 	    .add(sect.startTime);
 	}
 	@Override public Sect withKey(Sect sect, Long id) {
 	    return sect.withId(id);
 	}
     };
 
     public static final DaoAction<Block, Block> insertBlock = new SqlInsert<Block>("INSERT INTO block (sect_id, start_time) VALUES (?, ?)") {
 	@Override public ParameterSet withParameterSet(Block block) {
 	    return new ParameterSet()
 	    .add(block.sectId)
 	    .add(block.startTime);
 	}
 	@Override public Block withKey(Block block, Long id) {
 	    return block.withId(id);
 	}
     };
 
     public static final DaoAction<Symbol, Symbol> insertSymbol = new SqlInsert<Symbol>("INSERT INTO symbol (staff_id, block_id, start_time_numerator, start_time_denominator, symbol_type) VALUES (?, ?, ?, ?, ?)") {
 	@Override public ParameterSet withParameterSet(Symbol symbol) {
 	    return new ParameterSet()
 	    .add(symbol.staffId)
 	    .add(symbol.blockId)
 	    .add(symbol.startTime.getNumerator())
 	    .add(symbol.startTime.getDenominator())
 	    .add(symbol.symbolType.ordinal());
 	}
 	@Override public Symbol withKey(Symbol symbol, Long id) {
 	    return symbol.withId(id);
 	}
     };
 
     public static final DaoAction<Segment, Segment> insertSegment = new SqlInsert<Segment>("INSERT INTO segment (symbol_id, duration_numerator, duration_denominator) VALUES (?, ?, ?)") {
 	@Override public ParameterSet withParameterSet(Segment segment) {
 	    return new ParameterSet()
 	    .add(segment.symbolId)
 	    .add(segment.duration.getNumerator())
 	    .add(segment.duration.getDenominator());
 	}
 	@Override public Segment withKey(Segment segment, Long id) {
 	    return segment.withId(id);
 	}
     };
 
     public static final DaoAction<Note, Note> insertNote = new SqlInsert<Note>("INSERT INTO note (segment_id, note_name, octave, accidental) VALUES (?, ?, ?, ?)") {
 	@Override public ParameterSet withParameterSet(Note note) {
 	    return new ParameterSet()
 	    .add(note.segmentId)
 	    .add(note.pitch.noteName.ordinal())
 	    .add(note.pitch.octave)
 	    .add(note.accidental.ordinal());
 	}
 	@Override public Note withKey(Note note, Long id) {
 	    return note.withId(id);
 	}
     };
 
     public static final DaoAction<TimeSignature, TimeSignature> insertTimeSignature = new SqlInsert<TimeSignature>("INSERT INTO time_signature (symbol_id, beat_count, beat_value) VALUES (?, ?, ?)") {
 	@Override public ParameterSet withParameterSet(TimeSignature timeSignature) {
 	    return new ParameterSet()
 	    .add(timeSignature.symbolId)
 	    .add(timeSignature.beatCount)
 	    .add(timeSignature.beatValue.ordinal());
 	}
 	@Override public TimeSignature withKey(TimeSignature timeSignature, Long id) {
 	    return timeSignature.withId(id);
 	}
     };
 
     public static final DaoAction<KeySignature, KeySignature> insertKeySignature = new SqlInsert<KeySignature>("INSERT INTO key_signature (symbol_id, a, b, c, d, e, f, g) VALUES (?, ?, ?, ?, ?, ?, ?, ?)") {
 	@Override public ParameterSet withParameterSet(KeySignature keySignature) {
 	    return new ParameterSet()
 	    .add(keySignature.symbolId)
 	    .add(keySignature.a.ordinal())
 	    .add(keySignature.b.ordinal())
 	    .add(keySignature.c.ordinal())
 	    .add(keySignature.d.ordinal())
 	    .add(keySignature.e.ordinal())
 	    .add(keySignature.f.ordinal())
 	    .add(keySignature.g.ordinal());
 	}
 	@Override public KeySignature withKey(KeySignature keySignature, Long id) {
 	    return keySignature.withId(id);
 	}
     };
 
     public static final DaoAction<Symbol, Symbol> insertSymbolSegment = insertSymbol.chain(new DaoAction<Symbol, Symbol>() {
 	    @Override public Result<Symbol> run(Connection connection, final Symbol symbol) {
 		return insertSegment
 		.map(new Function1<Segment, Symbol>() {
 			public Symbol apply(Segment segment) {
 			    return symbol.withSegment(segment);
 			}
 		    })
 		.run(connection, symbol.segment);
 	    }
 	});
 
     public static final DaoAction<Symbol, Symbol> insertSymbolNote = insertSymbolSegment.chain(new DaoAction<Symbol, Symbol>() {
 	    @Override public Result<Symbol> run(Connection connection, final Symbol symbol) {
 		return insertNote
 		.map(new Function1<Note, Symbol>() {
 			public Symbol apply(Note note) {
 			    return symbol.withSegment(symbol.segment.withNote(note));
 			}
 		    })
 		.run(connection, symbol.segment.note);
 	    }
 	});
 
     public static final DaoAction<Symbol, Symbol> insertSymbolTimeSignature = insertSymbol.chain(new DaoAction<Symbol, Symbol>() {
 	    @Override public Result<Symbol> run(Connection connection, final Symbol symbol) {
 		return insertTimeSignature
 		.map(new Function1<TimeSignature, Symbol>() {
 			public Symbol apply(TimeSignature timeSignature) {
 			    return symbol.withTimeSignature(timeSignature);
 			}
 		    })
 		.run(connection, symbol.timeSignature);
 	    }
 	});
 
     public static final DaoAction<Symbol, Symbol> insertSymbolKeySignature = insertSymbol.chain(new DaoAction<Symbol, Symbol>() {
 	    @Override public Result<Symbol> run(Connection connection, final Symbol symbol) {
 		return insertKeySignature
 		.map(new Function1<KeySignature, Symbol>() {
 			public Symbol apply(KeySignature keySignature) {
 			    return symbol.withKeySignature(keySignature);
 			}
 		    })
 		.run(connection, symbol.keySignature);
 	    }
 	});
 
     public static final DaoAction<Symbol, Symbol> insertSymbolFull = new DaoAction<Symbol, Symbol>() {
 	@Override public Result<Symbol> run(Connection connection, Symbol symbol) {
 	    return new DaoAction[] {
 		insertSymbolSegment,
 		insertSymbolNote,
 		insertSymbol,
 		insertSymbolKeySignature,
 		insertSymbolTimeSignature
 	    }[symbol.symbolType.role.ordinal()].run(connection, symbol);
 	}
     };
 
     public static final DaoAction<Sect, Sect> insertSectWithBlocks = new DaoAction<Sect, Sect>() {
 	@Override public Result<Sect> run(Connection connection, final Sect sect) {
 	    return insertSect
 	    .chain(new DaoAction<Sect, Sect>() {
 		    @Override public Result<Sect> run(Connection connection, final Sect sect) {
 			return DaoAction.<Block, Block> runAll(connection, sect.blocks, insertBlock)
 			.map(new Function1<List<Block>, Sect>() {
 				public Sect apply(List<Block> blocks) {
 				    return sect.withBlocks(blocks);
 				}
 			    });
 		    }
 		})
 	    .run(connection, sect);
 	}
     };
 
     public static final DaoAction<Syst, Syst> insertSystWithStaves = new DaoAction<Syst, Syst>() {
 	public Result<Syst> run(Connection connection, final Syst syst) {
 	    return insertSyst
 	    .chain(new DaoAction<Syst, Syst>() {
 		    public Result<Syst> run(Connection connection, final Syst syst) {
 			return DaoAction.<Staff, Staff> runAll(connection, syst.staves, insertStaff)
 			.map(new Function1<List<Staff>, Syst>() {
 				public Syst apply(List<Staff> staves) {
 				    return syst.withStaves(staves);
 				}
 			    });
 		    }
 		})
 	    .run(connection, syst);
 	}
     };
 
     public static final DaoAction<Tune, Tune> insertTuneWithSystsAndSects = insertTune
 	.chain(new DaoAction<Tune, Tune>() {
 		public Result<Tune> run(Connection connection, final Tune tune) {
 		    return DaoAction.<Syst, Syst> runAll(connection, tune.systs, insertSystWithStaves)
 		    .map(new Function1<List<Syst>, Tune>() {
 			    public Tune apply(List<Syst> systs) {
 				return tune.withSysts(systs);
 			    }
 			});
 		}
 	    })
 	.chain(new DaoAction<Tune, Tune>() {
 		public Result<Tune> run(Connection connection, final Tune tune) {
 		    return DaoAction.<Sect, Sect> runAll(connection, tune.sects, insertSectWithBlocks)
 		    .map(new Function1<List<Sect>, Tune>() {
 			    public Tune apply(List<Sect> sects) {
 				return tune.withSects(sects);
 			    }
 			});
 		}
 	    })
 	.chain(new DaoAction<Tune, Tune>() {
 		public Result<Tune> run(Connection connection, Tune tune) {
 		    DaoAction<Tune, Tune> action = new DaoAction<Tune, Tune>() {
 			public Result<Tune> run(Connection connection, Tune tune) {
 			    return Result.<Tune> success(tune);
 			}
 		    };
 
 		    Map<Symbol, Sect> symbolSect = new HashMap<Symbol, Sect>();
 		    Map<Symbol, Block> symbolBlock = new HashMap<Symbol, Block>();
 
 		    for (Sect sect : tune.sects) {
 			for (Block block : sect.blocks) {
 			    for (Symbol symbol : block.symbols) {
 				symbolSect.put(symbol, sect);
 				symbolBlock.put(symbol, block);
 			    }
 			}
 		    }
 
 
 		    final Map<Symbol, Symbol> newSymbols = new HashMap<Symbol, Symbol>();
 
 		    for (final Syst syst : tune.systs) {
 			for (final Staff staff : syst.staves) {
 			    for (final Symbol symbol : staff.symbols) {
 				final Sect sect = symbolSect.get(symbol);
 				final Block block = symbolBlock.get(symbol);
 				action = action.chain(new DaoAction<Tune, Tune>() {
 					public Result<Tune> run(Connection connection, final Tune tune) {
 					    return insertSymbolFull
 						.run(connection, symbol
 						     .withStaffId(staff.id)
 						     .withBlockId(block.id))
 						.map(new Function1<Symbol, Tune>() {
 							public Tune apply(Symbol newSymbol) {
 							    newSymbols.put(symbol, newSymbol);
 							    return tune;
 							}
 						    });
 
 					}
 				    });
 			    }
 			}
 		    }
 
 		    action = action.map(new Function1<Tune, Tune>() {
 			    public Tune apply(Tune tune) {
 				for (Syst syst : tune.systs) {
 				    Syst newSyst = syst;
 				    for (Staff staff : syst.staves) {
 					Staff newStaff = staff;
 					for (Symbol symbol : staff.symbols) {
 					    newStaff = newStaff.replaceSymbol(symbol, newSymbols.get(symbol));
 					}
 					newSyst = newSyst.replaceStaff(staff, newStaff);
 				    }
 				    tune = tune.replaceSyst(syst, newSyst);
 				}
 				for (Sect sect : tune.sects) {
 				    Sect newSect = sect;
 				    for (Block block : sect.blocks) {
 					Block newBlock = block;
 					for (Symbol symbol : block.symbols) {
 					    newBlock = newBlock.replaceSymbol(symbol, newSymbols.get(symbol));
 					}
 					newSect = newSect.replaceBlock(block, newBlock);
 				    }
 				    tune = tune.replaceSect(sect, newSect);
 				}
 				return tune;
 			    }
 			});
 		    
 		    return action.run(connection, tune);
 		}
 	    });
 
     public static final DaoAction<Long, Tune> getTune = new SqlSelect<Long, Tune>("SELECT id, name FROM tune WHERE id = ?") {
 	public ParameterSet withParameterSet(Long id) {
 	    return new ParameterSet().add(id);
 	}
 
 	public RowParser<Tune> withRowParser(Long id) {
 	    return new FieldSet()
 	    .addLong()
 	    .addString()
 	    .toRowParser(new Function2<Long, String, Tune>() {
 		    public Tune apply(Long id, String name) {
 			return new Tune(id, name);
 		    }
 		});
 	}
     }.single();
 
     public static final DaoAction<Long, List<Staff>> getSystStaves = new SqlSelect<Long, Staff>("SELECT id, name FROM staff WHERE syst_id = ?") {
 	@Override public ParameterSet withParameterSet(Long systId) {
 	    return new ParameterSet().add(systId);
 	}
 
 	@Override public RowParser<Staff> withRowParser(final Long systId) {
 	    return new FieldSet()
 	    .addLong()
 	    .addString()
 	    .toRowParser(new Function2<Long, String, Staff>() {
 		    public Staff apply(Long id, String name) {
 			return new Staff(id, systId, name);
 		    }
 		});
 	}
     };
 
     public static final DaoAction<Long, List<Syst>> getTuneSysts = new SqlSelect<Long, Syst>("SELECT id, name FROM syst WHERE tune_id = ?") {
 	@Override public ParameterSet withParameterSet(Long tuneId) {
 	    return new ParameterSet().add(tuneId);
 	}
 
 	@Override public RowParser<Syst> withRowParser(final Long tuneId) {
 	    return new FieldSet()
 	    .addLong()
 	    .addString()
 	    .toRowParser(new Function2<Long, String, Syst>() {
 		    public Syst apply(Long id, String name) {
 			return new Syst(id, tuneId, name);
 		    }
 		});
 	}
     };
 
     public static final DaoAction<Long, List<Block>> getSectBlocks = new SqlSelect<Long, Block>("SELECT id, start_time FROM block WHERE sect_id = ?") {
 	@Override public ParameterSet withParameterSet(Long sectId) {
 	    return new ParameterSet().add(sectId);
 	}
 	@Override public RowParser<Block> withRowParser(final Long sectId) {
 	    return new FieldSet()
 	    .addLong()
 	    .addLong()
 	    .toRowParser(new Function2<Long, Long, Block>() {
 		    public Block apply(Long id, Long startTime) {
 			return new Block(id, sectId, startTime);
 		    }
 		});
 	}
     };
 
     public static final DaoAction<Long, List<Sect>> getTuneSects = new SqlSelect<Long, Sect>("SELECT id, start_time FROM sect WHERE tune_id = ?") {
 	@Override public ParameterSet withParameterSet(Long tuneId) {
 	    return new ParameterSet().add(tuneId);
 	}
 
 	@Override public RowParser<Sect> withRowParser(final Long tuneId) {
 	    return new FieldSet()
 	    .addLong()
 	    .addLong()
 	    .toRowParser(new Function2<Long, Long, Sect>() {
 		    public Sect apply(Long id, Long startTime) {
 			return new Sect(id, tuneId, startTime);
 		    }
 		});
 	}
     };
 
     public static final DaoAction<Long, Tune> getTuneWithSystsAndSects = getTune
 	.chain(new DaoAction<Tune, Tune>() {
 		public Result<Tune> run(Connection connection, final Tune tune) {
 		    return getTuneSysts
 		    .chain(new DaoAction<List<Syst>, List<Syst>>() {
 			    public Result<List<Syst>> run(Connection connection, List<Syst> systs) {
 				return DaoAction.<Syst, Syst> runAll(connection, systs, new DaoAction<Syst, Syst>() {
 					public Result<Syst> run(Connection connection, final Syst syst) {
 					    return getSystStaves
 					    .map(new Function1<List<Staff>, Syst>() {
 						    public Syst apply(List<Staff> staves) {
 							return syst.withStaves(staves);
 						    }
 						})
 					    .run(connection, syst.id);
 					}
 				    });
 			    }
 			})
 		    .map(new Function1<List<Syst>, Tune>() {
 			    public Tune apply(List<Syst> systs) {
 				return tune.withSysts(systs);
 			    }
 			})
 		    .run(connection, tune.id);
 		}
 	    })
 	.chain(new DaoAction<Tune, Tune>() {
 		public Result<Tune> run(Connection connection, final Tune tune) {
 		    return getTuneSects
 		    .chain(new DaoAction<List<Sect>, List<Sect>>() {
 			    public Result<List<Sect>> run(Connection connection, List<Sect> sects) {
 				return DaoAction.<Sect, Sect> runAll(connection, sects, new DaoAction<Sect, Sect>() {
 					public Result<Sect> run(Connection connection, final Sect sect) {
 					    return getSectBlocks
 					    .map(new Function1<List<Block>, Sect>() {
 						    public Sect apply(List<Block> blocks) {
 							return sect.withBlocks(blocks);
 						    }
 						})
 					    .run(connection, sect.id);
 					}
 				    });
 			    }
 			})
 		    .map(new Function1<List<Sect>, Tune>() {
 			    public Tune apply(List<Sect> sects) {
 				return tune.withSects(sects);
 			    }
 			})
 		    .run(connection, tune.id);
 		}
 	    });
 
     public static final DaoAction<Long, List<Symbol>> getBlockSymbols = new SqlSelect<Long, Symbol>("SELECT id, staff_id, start_time_numerator, start_time_denominator, symbol_type FROM symbol WHERE block_id = ?") {
 	public ParameterSet withParameterSet(Long blockId) {
 	    return new ParameterSet().add(blockId);
 	}
 	public RowParser<Symbol> withRowParser(final Long blockId) {
 	    return new FieldSet()
 	    .addLong()
 	    .addLong()
 	    .addInt()
 	    .addInt()
 	    .addInt()
 	    .toRowParser(new Function5<Long, Long, Integer, Integer, Integer, Symbol>() {
 		    public Symbol apply(Long id, Long staffId, Integer startTimeNumerator, Integer startTimeDenominator, Integer symbolType) {
 			return new Symbol(id, staffId, blockId, new Fraction(startTimeNumerator, startTimeDenominator), SymbolType.values()[symbolType], null, null, null);
 		    }
 		});
 	}
     };
 
     public static final DaoAction<Long, Segment> getSymbolSegment = new SqlSelect<Long, Segment>("SELECT id, duration_numerator, duration_denominator, dot_count FROM segment WHERE symbol_id = ?") {
 	public ParameterSet withParameterSet(Long symbolId) {
 	    return new ParameterSet().add(symbolId);
 	}
 	public RowParser<Segment> withRowParser(final Long symbolId) {
 	    return new FieldSet()
 	    .addLong()
 	    .addInt()
 	    .addInt()
 	    .addInt()
 	    .toRowParser(new Function4<Long, Integer, Integer, Integer, Segment>() {
 		    public Segment apply(Long id, Integer startTimeNumerator, Integer startTimeDenominator, Integer dotCount) {
 			return new Segment(id, symbolId, new Fraction(startTimeNumerator, startTimeDenominator), dotCount, null, null);
 		    }
 		});
 	}
     }.single();
 
     public static final DaoAction<Long, Note> getSegmentNote = new SqlSelect<Long, Note>("SELECT id, note_name, octave, accidental FROM note WHERE segment_id = ?") {
 	public ParameterSet withParameterSet(Long segmentId) {
 	    return new ParameterSet().add(segmentId);
 	}
 	public RowParser<Note> withRowParser(final Long segmentId) {
 	    return new FieldSet()
 	    .addLong()
 	    .addInt()
 	    .addInt()
 	    .addInt()
 	    .toRowParser(new Function4<Long, Integer, Integer, Integer, Note>() {
 		    public Note apply(Long id, Integer noteName, Integer octave, Integer accidental) {
 			return new Note(id, segmentId, new Pitch(NoteName.values()[noteName], octave), Accidental.values()[accidental]);
 		    }
 		});
 	}
     }.single();
 
    public static final DaoAction<Long, TimeSignature> getSymbolTimeSignature = new SqlSelect<Long, TimeSignature>("SELECT id, beat_count, beat_value FROM time_signature WHERE symbol_id = ?") {
 	public ParameterSet withParameterSet(Long symbolId) {
 	    return new ParameterSet().add(symbolId);
 	}
 	public RowParser<TimeSignature> withRowParser(final Long symbolId) {
 	    return new FieldSet()
 	    .addLong()
 	    .addInt()
 	    .addInt()
 	    .toRowParser(new Function3<Long, Integer, Integer, TimeSignature>() {
 		    public TimeSignature apply(Long id, Integer beatCount, Integer beatValue) {
 			return new TimeSignature(id, symbolId, beatCount, NoteValue.values()[beatValue]);
 		    }
 		});
 	}
     }.single();
 
     public static final DaoAction<Long, KeySignature> getSymbolKeySignature = new SqlSelect<Long, KeySignature>("SELECT id, a, b, c, d, e, f, g FROM key_signature WHERE symbol_id = ?") {
 	public ParameterSet withParameterSet(Long symbolId) {
 	    return new ParameterSet().add(symbolId);
 	}
 	public RowParser<KeySignature> withRowParser(final Long symbolId) {
 	    return new FieldSet()
 	    .addLong()
 	    .addInt()
 	    .addInt()
 	    .addInt()
 	    .addInt()
 	    .addInt()
 	    .addInt()
 	    .addInt()
 	    .toRowParser(new Function8<Long, Integer, Integer, Integer, Integer, Integer, Integer, Integer, KeySignature>() {
 		    public KeySignature apply(Long id, Integer a, Integer b, Integer c, Integer d, Integer e, Integer f, Integer g) {
 			return new KeySignature(id, symbolId,
 						KeySignature.Modifier.values()[a],
 						KeySignature.Modifier.values()[b],
 						KeySignature.Modifier.values()[c],
 						KeySignature.Modifier.values()[d],
 						KeySignature.Modifier.values()[e],
 						KeySignature.Modifier.values()[f],
 						KeySignature.Modifier.values()[g]);
 		    }
 		});
 	}
     }.single();
 
     public static final DaoAction<Long, List<Symbol>> getBlockSymbolsFull = getBlockSymbols
 	.chain(new DaoAction<List<Symbol>, List<Symbol>>() {
 		public Result<List<Symbol>> run(Connection connection, List<Symbol> symbols) {
 		    DaoAction<List<Symbol>, List<Symbol>> action = new DaoAction<List<Symbol>, List<Symbol>>() {
 			public Result<List<Symbol>> run(Connection connection, List<Symbol> symbols) {
 			    return Result.<List<Symbol>> success(symbols);
 			}
 		    };
 		    for (final Symbol symbol : symbols) {
 			DaoAction<Symbol, Symbol> newSymbolAction = new DaoAction<Symbol, Symbol>() {
 			    public Result<Symbol> run(Connection connection, Symbol symbol) {
 				return Result.<Symbol> success(symbol);
 			    }
 			};
 			switch(symbol.symbolType.role) {
 			case KEY_SIGNATURE:
 			    newSymbolAction = new DaoAction<Symbol, Symbol>() {
 				public Result<Symbol> run(Connection connection, final Symbol symbol) {
 				    return getSymbolKeySignature.map(new Function1<KeySignature, Symbol>() {
 					    public Symbol apply(KeySignature keySignature) {
 						return symbol.withKeySignature(keySignature);
 					    }
 					}).run(connection, symbol.id);
 				}
 			    };
 			    break;
 			case TIME_SIGNATURE:
 			    newSymbolAction = new DaoAction<Symbol, Symbol>() {
 				public Result<Symbol> run(Connection connection, final Symbol symbol) {
 				    return getSymbolTimeSignature.map(new Function1<TimeSignature, Symbol>() {
 					    public Symbol apply(TimeSignature timeSignature) {
 						return symbol.withTimeSignature(timeSignature);
 					    }
 					}).run(connection, symbol.id);
 				}
 			    };
 			    break;
 			case NOTE:
 			    newSymbolAction = new DaoAction<Symbol, Symbol>() {
 				public Result<Symbol> run(Connection connection, final Symbol symbol) {
 				    return getSymbolSegment.chain(new DaoAction<Segment, Segment>() {
 					    public Result<Segment> run(Connection connection, final Segment segment) {
 						return getSegmentNote.map(new Function1<Note, Segment>() {
 							public Segment apply(Note note) {
 							    return segment.withNote(note);
 							}
 						    }).run(connection, segment.id);
 					    }
 					}).map(new Function1<Segment, Symbol>() {
 					    public Symbol apply(Segment segment) {
 						return symbol.withSegment(segment);
 					    }
 					}).run(connection, symbol.id);
 				}
 			    };
 			    break;
 			case REST:
 			    newSymbolAction = new DaoAction<Symbol, Symbol>() {
 				public Result<Symbol> run(Connection connection, final Symbol symbol) {
 				    return getSymbolSegment.map(new Function1<Segment, Symbol>() {
 					    public Symbol apply(Segment segment) {
 						return symbol.withSegment(segment);
 					    }
 					}).run(connection, symbol.id);
 				}
 			    };
 			    break;
 			default:
 			    break;
 			}
 			final DaoAction<Symbol, Symbol> expandSymbol = newSymbolAction;
 			action = action.chain(new DaoAction<List<Symbol>, List<Symbol>>() {
 				public Result<List<Symbol>> run(Connection connection, final List<Symbol> symbols) {
 				    return expandSymbol.run(connection, symbol).map(new Function1<Symbol, List<Symbol>>() {
 					    public List<Symbol> apply(Symbol symbol) {
 						symbols.add(symbol);
 						return symbols;
 					    }
 					});
 				}
 			    });
 		    }
 		    return action.run(connection, new ArrayList<Symbol>());
 		}
 	    });
 
     public static final DaoAction<Void, List<Tune>> getAllTunes = new SqlSelect<Void, Tune>("SELECT id, name FROM tune") {
 	public ParameterSet withParameterSet(Void v) {
 	    return new ParameterSet();
 	}
 
 	public RowParser<Tune> withRowParser(Void v) {
 	    return new FieldSet()
 	    .addLong()
 	    .addString()
 	    .toRowParser(new Function2<Long, String, Tune>() {
 		    public Tune apply(Long id, String name) {
 			return new Tune(id, name);
 		    }
 		});
 	}
     };
 }
